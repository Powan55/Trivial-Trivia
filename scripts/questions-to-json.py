#!/usr/bin/env python3
"""Convert the shipped question CSV into the JSON the static demo fetches.

The demo on GitHub Pages has no server, so it cannot use the app's own CSV adapter. Running this
in the Pages workflow instead of committing the JSON is what stops the two copies drifting: add a
question to the CSV and the demo picks it up on the next deploy.

The output is the same array-of-rows shape the app's own JSON export produces, so a file from
/questions/export?format=json can be dropped straight in.

    python scripts/questions-to-json.py <input.csv> <output.json>
"""

import csv
import json
import sys

COLUMNS = 6  # question, four options, answer


def main(source, target):
    with open(source, newline="", encoding="utf-8") as handle:
        rows = [row for row in csv.reader(handle) if len(row) >= COLUMNS]

    if len(rows) < 2:
        sys.exit(f"{source}: expected a header and at least one question, found {len(rows)} rows")

    with open(target, "w", encoding="utf-8") as handle:
        json.dump(rows, handle, ensure_ascii=False, indent=2)

    print(f"{target}: {len(rows) - 1} questions")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2])
