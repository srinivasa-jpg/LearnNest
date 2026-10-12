#!/usr/bin/env python3
"""Convert .docx files in word-course-inbox into offline HTML and image assets.

Run: py -3 tools/import_word_courses.py word-course-inbox app/src/main/assets/word_courses
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
CSS = """*{box-sizing:border-box}html{font-size:17px;-webkit-text-size-adjust:100%}
body{font-family:system-ui,Roboto,Arial,sans-serif;background:#f7f8fc;color:#202c40;line-height:1.6;margin:0}
main{max-width:850px;margin:0 auto;padding:16px 16px 40px}h1{font-size:1.4rem;color:#203d79}
p{margin:0 0 14px;overflow-wrap:anywhere}figure{margin:10px 0 20px;padding:8px;border-radius:10px;background:white;border:1px solid #dae0e9}
img{display:block;width:auto;max-width:100%;height:auto;margin:auto}table{border-collapse:collapse;width:100%}
td{border:1px solid #d3d8e0;padding:8px}strong{font-weight:700}"""


def document_id(name):
    base = re.sub(r"[^a-z0-9]+", "-", name.lower()).strip("-")[:44] or "lesson"
    checksum = hashlib.sha256(name.casefold().encode("utf-8")).hexdigest()[:8]
    return base + "-" + checksum


def parse_document(path, destination):
    key = document_id(path.stem)
    paragraphs = []
    files = []
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
                return "<p>[Missing embedded image]</p>"
            extension = Path(source).suffix.lower()
            if extension not in ALLOWED:
                return "<p>[Unsupported image format: " + html.escape(extension) + "]</p>"
            image_name = "image-" + hashlib.sha256(source.encode()).hexdigest()[:12] + extension
            target = destination / "images" / key / image_name
            target.parent.mkdir(parents=True, exist_ok=True)
            if not target.exists():
                target.write_bytes(archive.read(source))
                files.append(image_name)
            relative = "../images/" + key + "/" + image_name
            return '<figure><img alt="Embedded diagram" src="' + relative + '"></figure>'

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
                paragraphs.append("<p>" + "".join(spans) + "</p>")

    document_path = destination / "documents" / (key + ".html")
    document_path.parent.mkdir(parents=True, exist_ok=True)
    page = ('<!doctype html><html><head><meta charset="utf-8">'
            '<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=5">'
            '<link rel="stylesheet" href="../reader.css"></head><body><main><h1>'
            + html.escape(path.stem) + '</h1>' + "\n".join(paragraphs)
            + '</main></body></html>')
    document_path.write_text(page, encoding="utf-8")
    return {"id": key, "title": path.stem,
            "htmlAsset": "word_courses/documents/" + key + ".html",
            "imageCount": len(files)}


def main():
    if len(sys.argv) != 3:
        print("Usage: import_word_courses.py INPUT_FOLDER GENERATED_ASSETS", file=sys.stderr)
        return 2
    inbox, destination = Path(sys.argv[1]), Path(sys.argv[2])
    inbox.mkdir(parents=True, exist_ok=True)
    documents = sorted(p for p in inbox.iterdir() if p.is_file() and p.suffix.lower() == ".docx"
                       and not p.name.startswith("~$"))
    # Only this importer should write to GENERATED_ASSETS.
    if destination.exists():
        shutil.rmtree(destination)
    destination.mkdir(parents=True)
    (destination / "reader.css").write_text(CSS, encoding="utf-8")
    entries = []
    for file in documents:
        print("Importing " + file.name)
        entries.append(parse_document(file, destination))
    (destination / "index.json").write_text(
        json.dumps({"version": 1, "documents": entries}, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8")
    print("Imported " + str(len(entries)) + " Word documents")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
