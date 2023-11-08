#!/bin/bash
pip3 install mkdocs-material
git fetch
mkdocs gh-deploy --config-file mkdocs/mkdocs.yml