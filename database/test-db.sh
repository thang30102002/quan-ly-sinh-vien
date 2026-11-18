#!/bin/bash
# Tạo database qlsv_test cho JUnit (#35): chỉ có bảng, không có dữ liệu mẫu.
# Test DAO xóa dữ liệu trước và sau mỗi test, nên không được chạy trên database qlsv.
#
# Docker chạy file này sau schema.sql và seed.sql (theo thứ tự tên file), và chỉ chạy
# khi volume còn trống. Với volume đã có sẵn, xem các lệnh tạo tay trong docs/docker.md.
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
     -c "CREATE DATABASE qlsv_test"
psql -v ON_ERROR_STOP=1 --quiet --username "$POSTGRES_USER" --dbname qlsv_test \
     -f /docker-entrypoint-initdb.d/schema.sql
