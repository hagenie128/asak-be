-- menu.image_asset_id가 이미 존재하는 DB의 기존 image_url 연결 보정 SQL.
-- 이 파일은 애플리케이션에서 자동 실행되지 않는다.

-- 기존 URL과 일치하는 활성 자산을 연결한다.
-- 동일 URL이 여러 행에 있을 때는 가장 작은 asset id만 선택한다.
UPDATE menu m
JOIN (
  SELECT url, MIN(id) AS id
  FROM media_asset
  WHERE deleted_at IS NULL
  GROUP BY url
) ma ON ma.url = m.image_url
SET m.image_asset_id = ma.id
WHERE m.image_url IS NOT NULL
  AND m.image_url <> ''
  AND m.image_asset_id IS NULL;

-- 아래 결과가 0건인지 확인한다. 남은 행은 media_asset 등록 후 다시 실행한다.
SELECT m.id, m.name, m.image_url
FROM menu m
WHERE m.image_url IS NOT NULL
  AND m.image_url <> ''
  AND m.image_asset_id IS NULL;

-- 검증이 끝날 때까지 menu.image_url은 삭제하지 않는다.
-- 애플리케이션은 image_asset_id와 media_asset.url만 사용한다.
