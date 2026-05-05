import subprocess
import xml.etree.ElementTree as ET
import json
from datetime import datetime
import os

PACKAGE = "com.yasumo.locationlambda"
PREF_PATH = "shared_prefs/location_lambda_debug_logs.xml"

OUTPUT_DIR = r"C:\githubClone\LocationLambda\debug-logs"

TYPE_MAP = {
    "NOTIFICATION": "noti",
    "GEOFENCE": "geof",
    "IGNORED": "igno",
    "REGISTRATION": "regi",
    "PERMISSION": "perm",
    "RECEIVED": "recv",
    "SUPPRESSED": "supp",
    "RULE": "rule",
    "STATUS": "stat",
    "RESTORE": "boot",
    "MARKER": "mark",
}


def get_xml_from_device():
    result = subprocess.run(
        ["adb", "shell", "run-as", PACKAGE, "cat", PREF_PATH],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )

    if result.returncode != 0 or not result.stdout or not result.stdout.strip():
        raise Exception(f"ログ取得失敗 returncode={result.returncode}\n{result.stderr}")

    return result.stdout


def parse_logs(xml_str):
    root = ET.fromstring(xml_str)

    for elem in root.findall("string"):
        if elem.attrib.get("name") == "logs":
            return json.loads(elem.text) if elem.text else []

    return []


def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    now = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_path = os.path.join(OUTPUT_DIR, f"debuglog_{now}.log")

    xml_str = get_xml_from_device()
    logs = parse_logs(xml_str)

    logs.sort(key=lambda x: x["timestampMillis"], reverse=True)

    with open(output_path, "w", encoding="utf-8") as f:
        for log in logs:
            dt = datetime.fromtimestamp(log["timestampMillis"] / 1000)
            time_str = dt.strftime("%Y/%m/%d %H:%M:%S")
            type_str = TYPE_MAP.get(log.get("type"), "unknown")
            title = log.get("title", "")
            detail = log.get("detail", "")

            line = f"{time_str} {type_str} {title} / {detail}"
            f.write(line + "\n")

    print(f"出力: {output_path}")


if __name__ == "__main__":
    main()
