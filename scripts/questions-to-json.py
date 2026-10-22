#!/usr/bin/env python3
"""Convert the shipped question CSV into the JSON the static demo fetches.

The demo on GitHub Pages has no server, so it cannot use the app's own CSV adapter. Running this
in the Pages workflow instead of committing the JSON is what stops the two copies drifting: add a
question to the CSV and the demo picks it up on the next deploy.

The output is the same array-of-rows shape the app's own JSON export produces, so a file from
/api/v1/questions/export?format=json can be dropped straight in.

    python scripts/questions-to-json.py <input.csv> <output.json>
"""

import csv
import json
import sys

LEGACY_HEADER = ["Question", "Option1", "Option2", "Option3", "Option4", "Answer"]
HEADER = LEGACY_HEADER + ["Category", "Difficulty"]


def main(source, target):
    with open(source, newline="", encoding="utf-8") as handle:
        rows = list(csv.reader(handle))
    if not rows or rows[0] not in (LEGACY_HEADER, HEADER):
        sys.exit(f"{source}: expected a six-column or eight-column question header")
    width = len(rows[0])
    normalized = [HEADER]
    for number, row in enumerate(rows[1:], 2):
        if len(row) != width or any(not cell.strip() for cell in row[:6]):
            sys.exit(f"{source}:{number}: invalid question row")
        if row[5] not in row[1:5]:
            sys.exit(f"{source}:{number}: answer must match an option")
        category = (row[6].strip() if width == 8 else "") or "Uncategorized"
        difficulty = ((row[7].strip() if width == 8 else "") or "MEDIUM").upper()
        if len(category) > 60 or difficulty not in ("EASY", "MEDIUM", "HARD"):
            sys.exit(f"{source}:{number}: invalid category or difficulty")
        normalized.append(row[:6] + [category, difficulty])
    if len(normalized) < 2:
        sys.exit(f"{source}: expected at least one question")
    rows = normalized

    with open(target, "w", encoding="utf-8") as handle:
        json.dump(rows, handle, ensure_ascii=False, indent=2)

    print(f"{target}: {len(rows) - 1} questions")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2])
