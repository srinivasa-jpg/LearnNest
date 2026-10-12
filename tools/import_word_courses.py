#!/usr/bin/env python3
"""Import Subjects / Units / Topics from course-content into offline HTML and PDF assets.

Run: py -3 tools/import_word_courses.py course-content app/src/main/assets/generated_courses
Only the Python standard library is required. Generated files are rewritten per run.
"""
import hashlib
import html
import json
import re
import shutil
import sys
from pathlib import Path
from xml.etree import ElementTree as ET
from zipfile import ZipFile

W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
A = "http://schemas.openxmlformats.org/drawingml/2006/main"
R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
V = "urn:schemas-microsoft-com:vml"
PKG = "http://schemas.openxmlformats.org/package/2006/relationships"
T = lambda ns, name: "{" + ns + "}" + name
ALLOWED = {".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp"}
CSS = """*{box-sizing:border-box}
html{font-size:16px;-webkit-text-size-adjust:100%;background:#f7f8fc}
body{font-family:system-ui,Roboto,Arial,sans-serif;line-height:1.5;color:#202c40;margin:0}
main{max-width:800px;margin:0 auto;padding:8px 16px 30px}
p{margin:0 0 9px;overflow-wrap:anywhere}
h2{font-size:1.15rem;line-height:1.35;margin:14px 0 8px;color:#203d79}
h3{font-size:1.04rem;line-height:1.4;margin:12px 0 6px;color:#203d79}
.question{display:grid;grid-template-columns:1.7em minmax(0,1fr);column-gap:7px;align-items:start;margin:11px 0 15px}
.question-number{font-weight:650;white-space:nowrap;line-height:1.5}
.question-text{margin:0;line-height:1.5}
img{display:block;max-width:100%;width:auto;height:auto;margin:10px auto}
table{border-collapse:collapse;width:100%}td{border:1px solid #d3d8e0;padding:8px}
strong{font-weight:700}"""


def document_id(name):
    base = re.sub(r"[^a-z0-9]+", "-", name.lower()).strip("-")[:44] or "lesson"
    checksum = hashlib.sha256(name.casefold().encode("utf-8")).hexdigest()[:8]
    return base + "-" + checksum


def parse_document(path, destination, stable_key):
    key = document_id(stable_key)
    paragraphs = []
    files = []
    pending_number = None
    with ZipFile(path) as archive:
        members = set(archive.namelist())
        if "word/document.xml" not in members:
            raise ValueError("Not a valid .docx file: " + path.name)
        root = ET.fromstring(archive.read("word/document.xml"))
        rels = {}
        rels_file = "word/_rels/document.xml.rels"
        if rels_file in members:
            for entry in ET.fromstring(archive.read(rels_file)):
                if entry.attrib.get("TargetMode") == "External":
                    continue
                target = entry.attrib.get("Target", "")
                if target.startswith("/word/"):
                    asset = target.lstrip("/")
                elif target.startswith("media/"):
                    asset = "word/" + target
                else:
                    continue
                rels[entry.attrib.get("Id", "")] = asset

        def image_markup(rel_id):
            source = rels.get(rel_id)
            if not source or source not in members:
                return "[Missing embedded image]"
            extension = Path(source).suffix.lower()
            if extension not in ALLOWED:
                return "[Unsupported image format: " + html.escape(extension) + "]"
            image_name = "image-" + hashlib.sha256(source.encode()).hexdigest()[:12] + extension
            target = destination / "images" / key / image_name
            target.parent.mkdir(parents=True, exist_ok=True)
            if not target.exists():
                target.write_bytes(archive.read(source))
                files.append(image_name)
            relative = "../images/" + key + "/" + image_name
            return '<img class="diagram" alt="Embedded diagram" src="' + relative + '">'

        body = root.find(T(W, "body"))
        if body is None:
            raise ValueError("Word document has no body: " + path.name)
        # Includes paragraphs inside tables, in their original document order.
        for paragraph in body.iter(T(W, "p")):
            spans = []
            for item in paragraph.iter():
                if item.tag == T(W, "t"):
                    spans.append(html.escape(item.text or ""))
                elif item.tag == T(W, "tab"):
                    spans.append("&emsp;")
                elif item.tag in (T(W, "br"), T(W, "cr")):
                    spans.append("<br>")
                elif item.tag == T(A, "blip"):
                    spans.append(image_markup(item.attrib.get(T(R, "embed"), "")))
                elif item.tag == T(V, "imagedata"):
                    spans.append(image_markup(item.attrib.get(T(R, "id"), "")))
            if spans:
                raw = "".join(spans).strip()
                if not raw or not re.sub(r"<[^>]+>", "", raw).strip() and "<img" not in raw:
                    continue
                style = paragraph.find(".//" + T(W, "pStyle"))
                heading = style.attrib.get(T(W, "val"), "") if style is not None else ""
                tag = "h2" if heading.startswith(("Heading1", "heading1")) else (
                    "h3" if heading.startswith(("Heading2", "heading2")) else "p")
                # Word often puts "1." on its own paragraph followed by the question.
                # Put the number and its text on the same visual row without rewriting content.
                number_only = re.fullmatch(r"(\\d{1,3}[.)])", html.unescape(raw))
                if tag == "p" and number_only:
                    if pending_number is not None:
                        paragraphs.append('<p class="question-number">' + pending_number + "</p>")
                    pending_number = html.escape(number_only.group(1))
                    continue
                if pending_number is not None:
                    paragraphs.append(
                        '<div class="question"><span class="question-number">' +
                        pending_number + '</span><p class="question-text">' +
                        raw + "</p></div>"
                    )
                    pending_number = None
                    continue
                inline_number = re.match(r"^(\\d{1,3}[.)])(?:\\s|&emsp;)+(.+)$", raw, re.S)
                if tag == "p" and inline_number:
                    paragraphs.append(
                        '<div class="question"><span class="question-number">' +
                        html.escape(inline_number.group(1)) +
                        '</span><p class="question-text">' +
                        inline_number.group(2) + "</p></div>"
                    )
                else:
                    paragraphs.append("<" + tag + ">" + raw + "</" + tag + ">")
        if pending_number is not None:
            paragraphs.append('<p class="question-number">' + pending_number + "</p>")

    document_path = destination / "documents" / (key + ".html")
    document_path.parent.mkdir(parents=True, exist_ok=True)
    page = ('<!doctype html><html><head><meta charset="utf-8">'
            '<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=5">'
            '<link rel="stylesheet" href="../reader.css"></head><body><main>'
            + "\n".join(paragraphs)
            + '</main></body></html>')
    document_path.write_text(page, encoding="utf-8")
    return {"id": key, "title": path.stem,
            "type": "word",
            "asset": "generated_courses/documents/" + key + ".html",
            "imageCount": len(files)}


def main():
    if len(sys.argv) != 3:
        print("Usage: import_word_courses.py COURSE_CONTENT_FOLDER GENERATED_ASSETS", file=sys.stderr)
        return 2
    source, destination = Path(sys.argv[1]), Path(sys.argv[2])
    source.mkdir(parents=True, exist_ok=True)
    if source.resolve() == destination.resolve() or source.resolve() in destination.resolve().parents:
        raise ValueError("Generated assets cannot be placed inside the source folder.")
    # Only this importer writes generated_courses. It owns and rewrites the whole directory.
    if destination.exists():
        shutil.rmtree(destination)
    destination.mkdir(parents=True)
    (destination / "reader.css").write_text(CSS, encoding="utf-8")
    subjects = []
    for subject_dir in sorted((p for p in source.iterdir() if p.is_dir() and not p.name.startswith(".")),
                              key=lambda p: p.name.casefold()):
        units = []
        for unit_dir in sorted((p for p in subject_dir.iterdir() if p.is_dir() and not p.name.startswith(".")),
                               key=lambda p: p.name.casefold()):
            topics = []
            for document in sorted(unit_dir.iterdir(), key=lambda p: p.name.casefold()):
                if not document.is_file() or document.name.startswith("~$"):
                    continue
                ext = document.suffix.lower()
                if ext not in (".docx", ".pdf"):
                    continue
                stable_key = document.relative_to(source).as_posix()
                if ext == ".docx":
                    item = parse_document(document, destination, stable_key)
                else:
                    key = document_id(stable_key)
                    pdf_dir = destination / "pdfs"
                    pdf_dir.mkdir(parents=True, exist_ok=True)
                    shutil.copyfile(document, pdf_dir / (key + ".pdf"))
                    item = {
                        "id": key, "title": document.stem, "type": "pdf",
                        "asset": "generated_courses/pdfs/" + key + ".pdf",
                    }
                topics.append(item)
                print("Imported " + stable_key)
            if topics:
                units.append({
                    "id": document_id(subject_dir.name + "/" + unit_dir.name),
                    "title": unit_dir.name, "topics": topics
                })
        if units:
            subjects.append({
                "id": document_id(subject_dir.name),
                "title": subject_dir.name, "units": units
            })
    (destination / "index.json").write_text(
        json.dumps({"version": 2, "subjects": subjects}, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8")
    print("Imported " + str(sum(len(unit["topics"]) for sub in subjects for unit in sub["units"])) +
          " topics in " + str(len(subjects)) + " subjects")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
