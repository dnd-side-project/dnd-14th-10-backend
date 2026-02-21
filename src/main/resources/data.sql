INSERT INTO tags (code, name, created_at, updated_at) VALUES
('CLEAN', '청결해요', NOW(), NOW()),
('PARTITION', '칸막이가 있어요', NOW(), NOW()),
('FOCUS', '집중하기 좋아요', NOW(), NOW()),
('VALUE', '가성비가 좋아요', NOW(), NOW()),
('KIND_OWNER', '사장님이 친절해요', NOW(), NOW()),
('GROUP_WORK', '단체 작업하기 좋아요', NOW(), NOW()),
('WIDE_SEAT_GAP', '좌석 간 간격이 넓어요', NOW(), NOW()),
('WIDE_SPACE', '작업공간이 넓어요', NOW(), NOW()),
('COZY', '아늑해요', NOW(), NOW()),
('CLEAN_RESTROOM', '화장실이 깨끗해요', NOW(), NOW()),
('MANY_SEATS', '자리가 많아요', NOW(), NOW()),
('COMFORTABLE_SEAT', '좌석이 편해요', NOW(), NOW()),
('WORTH_PRICE', '비싼 만큼 가치있어요', NOW(), NOW()),
('LONG_STAY', '오래 머무르기 좋아요', NOW(), NOW()),
('PRETTY_INTERIOR', '인테리어가 예뻐요', NOW(), NOW()),
('GOOD_VIEW', '뷰가 좋아요', NOW(), NOW()),
('ADULT_ONLY', '어른만의 공간이에요', NOW(), NOW()),
('BRIGHT_LIGHT', '조명이 밝았어요', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO badges (code, name, description, created_at, updated_at) VALUES
('REVIEW_1', '첫 리뷰', '첫 번째 리뷰를 작성했습니다', NOW(), NOW()),
('REVIEW_25', '리뷰 25개', '리뷰를 25개 작성했습니다', NOW(), NOW()),
('REVIEW_80', '리뷰 80개', '리뷰를 80개 작성했습니다', NOW(), NOW()),
('PLACE_1', '첫 장소', '첫 번째 장소를 등록했습니다', NOW(), NOW()),
('PLACE_7', '장소 7개', '장소를 7개 등록했습니다', NOW(), NOW()),
('PLACE_20', '장소 20개', '장소를 20개 등록했습니다', NOW(), NOW()),
('IMAGE_5', '이미지 5개', '이미지가 포함된 활동을 5회 했습니다', NOW(), NOW()),
('IMAGE_30', '이미지 30개', '이미지가 포함된 활동을 30회 했습니다', NOW(), NOW()),
('IMAGE_80', '이미지 80개', '이미지가 포함된 활동을 80회 했습니다', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;
