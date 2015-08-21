# -*- encoding: utf-8 -*-
default_app_config = "quiz.apps.QuizConfig"

TFQUESTION='TF'
MCQUESTION='MC'
ESSAYQUESTION='ESSAY'

QUESTION_TYPES=(
               (TFQUESTION, 'Câu hỏi Đúng - Sai'),
               (MCQUESTION, 'Câu hỏi Multiple Choice'),
               (ESSAYQUESTION, 'Câu hỏi tự luận'),
               )

ANSWER_ORDER_OPTIONS = (
    ('CONTENT', 'Nội dung'),
    ('RANDOM', 'Ngẫu nhiên'),
    ('NONE', 'None')
)