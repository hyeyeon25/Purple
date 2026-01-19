#!/usr/bin/env bash
# 에러 나면 즉시 종료
set -o errexit

# 1. 파이썬 라이브러리 설치
pip install -r requirements.txt

# 2. 크롬 다운로드 및 압축 해제 (Render Native 환경용)
STORAGE_DIR=/opt/render/project/src/ai-server/chrome

echo "...Downloading Chrome"
mkdir -p $STORAGE_DIR
cd $STORAGE_DIR
wget -P ./ https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
dpkg -x ./google-chrome-stable_current_amd64.deb $STORAGE_DIR
rm ./google-chrome-stable_current_amd64.deb
cd /opt/render/project/src/ai-server # 다시 원래 위치로 복귀

echo "...Chrome installed successfully"