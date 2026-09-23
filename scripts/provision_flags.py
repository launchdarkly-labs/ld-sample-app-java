#!/usr/bin/env python3
"""Provision LaunchDarkly flags for this demo. Separate from the Java app.

Requires a LaunchDarkly REST API access token (api-...), not an SDK key.

  python3 scripts/provision_flags.py --api-key api-xxxxxxxx

Creates boolean flags in nteixeira-ld-custom and sets dashboard as a
prerequisite on the three UI flags.
"""

from __future__ import annotations

import argparse
import json
import sys
import urllib.error
import urllib.request

PROJECT = "nteixeira-ld-custom"
API = "https://app.launchdarkly.com/api/v2"
TAG = "java-sdk-demo"

FLAGS = [
    {
        "key": "dashboard",
        "name": "Dashboard",
        "description": "Keystone flag. Must be on (true) before dashboard UI flags can serve true.",
        "prerequisite": False,
    },
    {
        "key": "dashboard-progress-meters",
        "name": "Dashboard Progress Meters",
        "description": "Controls visibility of the dashboard progress meters.",
        "prerequisite": True,
    },
    {
        "key": "dashboard-line-chart",
        "name": "Dashboard Line Chart",
        "description": "Controls visibility of the dashboard line chart.",
        "prerequisite": True,
    },
    {
        "key": "dashboard-bar-chart",
        "name": "Dashboard Bar Chart",
        "description": "Controls visibility of the dashboard bar chart.",
        "prerequisite": True,
    },
]


def request(api_key: str, method: str, path: str, body=None, extra_headers=None):
    headers = {
        "Authorization": api_key,
        "Content-Type": "application/json",
    }
    if extra_headers:
        headers.update(extra_headers)
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(API + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            raw = resp.read()
            return resp.status, json.loads(raw) if raw else {}
    except urllib.error.HTTPError as exc:
        raw = exc.read()
        payload = {}
        if raw:
            try:
                payload = json.loads(raw)
            except json.JSONDecodeError:
                payload = {"message": raw.decode(errors="replace")}
        return exc.code, payload


def true_variation_id(flag: dict) -> str:
    for variation in flag.get("variations", []):
        if variation.get("value") is True:
            return variation["_id"]
    raise SystemExit("dashboard flag is missing a true variation")


def create_flag(api_key: str, spec: dict) -> None:
    status, payload = request(
        api_key,
        "POST",
        f"/flags/{PROJECT}",
        {
            "key": spec["key"],
            "name": spec["name"],
            "description": spec["description"],
            "kind": "boolean",
            "temporary": False,
            "tags": [TAG],
            "defaults": {"onVariation": 0, "offVariation": 1},
        },
    )
    if status in (200, 201):
        print(f"created {spec['key']}")
        return
    if status == 409:
        print(f"exists  {spec['key']}")
        return
    raise SystemExit(f"failed to create {spec['key']}: {status} {payload}")


def add_prerequisite(api_key: str, flag_key: str, env: str, variation_id: str) -> None:
    status, payload = request(
        api_key,
        "PATCH",
        f"/flags/{PROJECT}/{flag_key}",
        {
            "environmentKey": env,
            "comment": "Require dashboard=true before this UI flag can serve true.",
            "instructions": [
                {"kind": "addPrerequisite", "key": "dashboard", "variationId": variation_id}
            ],
        },
        extra_headers={"Content-Type": "application/json; domain-model=launchdarkly.semanticpatch"},
    )
    if status in (200, 201):
        print(f"prerequisite dashboard -> {flag_key} ({env})")
        return
    message = str(payload)
    if status == 400 and "already" in message.lower():
        print(f"prerequisite already set on {flag_key} ({env})")
        return
    if status == 405:
        print(f"skip    {flag_key} ({env}): environment requires approval")
        return
    raise SystemExit(f"failed prerequisite on {flag_key} ({env}): {status} {payload}")


def main() -> int:
    parser = argparse.ArgumentParser(description="Create LaunchDarkly flags for the Java SDK demo.")
    parser.add_argument(
        "--api-key",
        required=True,
        help="LaunchDarkly REST API access token (api-...), not an SDK key",
    )
    parser.add_argument(
        "--env",
        default="production",
        help="Environment for prerequisites (default: production)",
    )
    args = parser.parse_args()
    api_key = args.api_key.strip()
    if not api_key:
        print("API key is empty", file=sys.stderr)
        return 1

    for spec in FLAGS:
        create_flag(api_key, spec)

    status, dashboard = request(api_key, "GET", f"/flags/{PROJECT}/dashboard")
    if status != 200:
        raise SystemExit(f"failed to read dashboard flag: {status} {dashboard}")
    variation_id = true_variation_id(dashboard)

    for spec in FLAGS:
        if spec["prerequisite"]:
            add_prerequisite(api_key, spec["key"], args.env, variation_id)

    print(f"done. flags are in {PROJECT} ({args.env}). targeting starts OFF.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
