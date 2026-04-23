package com.deutschhub.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION("error.uncategorized", 9999),

    INVALID_EMAIL("error.identity.invalid.email", 3001),
    INVALID_ROLE_NAME("error.identity.invalid.role.name", 3002),
    INVALID_USERNAME("error.identity.invalid.username", 3003),
    INVALID_PASSWORD("error.identity.invalid.password", 3004),

    USER_ALREADY_EXISTS("error.user.already.exists", 4001),

    INVALID_COURSE_TITLE("error.course.invalid.title", 5001),
    INVALID_CEFR_LEVEL("error.course.invalid.level", 5002),
    INVALID_COURSE_PRICE("error.course.invalid.price", 5003),
    COURSE_HAS_NO_SECTIONS("error.course.has.no.sections", 5004),
    COURSE_ALREADY_PUBLISHED("error.course.already.published", 5005),
    CANNOT_MODIFY_PUBLISHED_COURSE("error.course.cannot.modify.published", 5006),
    INVALID_LESSON("error.course.invalid.lesson", 5007),
    INVALID_COURSE_INSTRUCTOR("error.course.invalid.instructor", 5008),
    COURSE_ALREADY_DELETED("error.course.already.deleted", 5009),
    COURSE_NOT_COMPLETED("error.course.not.completed", 5011),

    INVALID_LESSON_ORDER("error.lesson.invalid.order", 6001),
    LESSON_INVALID_TITLE("error.lesson.invalid.title", 6002),
    LESSON_INVALID_DURATION("error.lesson.invalid.duration", 6003),
    LESSON_INVALID_CONTENT("error.lesson.invalid.content", 6004),

    SECTION_INVALID_TITLE("error.section.invalid.title", 7001),
    INVALID_SECTION_ORDER("error.section.invalid.order", 7002),
    SECTION_NOT_FOUND("error.section.not.found", 7003),

    INVALID_ENROLLMENT_DATA("error.enrollment.invalid.data", 8001),
    CANNOT_START_COMPLETED_ENROLLMENT("error.enrollment.cannot.start.completed", 8002),
    CANNOT_DROP_COMPLETED_ENROLLMENT("error.enrollment.cannot.drop.completed", 8003),
    INVALID_ENROLLMENT_STATE("error.enrollment.invalid.state", 8004),
    ENROLLMENT_NOT_ACTIVE("error.enrollment.not.active", 8005),
    ENROLLMENT_ALREADY_DELETED("error.enrollment.already.deleted", 8006),
    ENROLLMENT_PROGRESS_CANNOT_DECREASE("error.enrollment.progress.cannot.decrease", 8101),
    INVALID_ENROLLMENT_PROGRESS_STATE("error.enrollment.progress.state", 8102),

    INVALID_PROGRESS_DATA("error.enrollment.progress.invalid.data", 8103),

    INVALID_USER_PROGRESS_DATA("error.userprogress.invalid.data", 9001),
    USER_PROGRESS_ALREADY_COMPLETED("error.userprogress.already.completed", 9002),
    USER_PROGRESS_ALREADY_DELETED("error.userprogress.already.deleted", 9003),
    PROGRESS_CANNOT_DECREASE("error.userprogress.progress.cannot.decrease", 9101),


    QUIZ_INVALID_TITLE("error.quiz.invalid.title", 10001),
    QUIZ_INVALID_TIME_LIMIT("error.quiz.invalid.time.limit", 10002),
    QUIZ_INVALID_MAX_SCORE("error.quiz.invalid.max.score", 10003),
    QUIZ_INVALID_PASSING_SCORE("error.quiz.invalid.passing.score", 10004),
    QUIZ_HAS_NO_QUESTIONS("error.quiz.has.no.questions", 10005),
    QUIZ_ALREADY_PUBLISHED("error.quiz.already.published", 10006),
    CANNOT_MODIFY_PUBLISHED_QUIZ("error.quiz.cannot.modify.published", 10007),
    INVALID_QUESTION("error.quiz.invalid.question", 10008),
    QUIZ_SCORE_MISMATCH("error.quiz.score.mismatch", 10009),
    INVALID_QUIZ_STATE("error.quiz.invalid.state", 10010),
    QUIZ_SCORE_EXCEEDS_MAX("error.quiz.score.exceeds.max", 10011),
    QUIZ_ALREADY_DELETED("error.quiz.already.deleted", 10012),
    QUIZ_REQUIREMENT_NOT_MET("error.quiz.requirement.not.met", 10013),

    INVALID_QUESTION_CONTENT("error.question.invalid.content", 11001),
    INVALID_QUESTION_SCORE("error.question.invalid.score", 11002),
    QUESTION_NOT_ENOUGH_ANSWERS("error.question.not.enough.answers", 11003),
    QUESTION_NO_CORRECT_ANSWER("error.question.no.correct.answer", 11004),
    QUESTION_TOO_MANY_CORRECT_ANSWERS("error.question.too.many.correct.answers", 11005),
    QUESTION_NOT_FOUND("error.question.not.found", 11006),
    DUPLICATE_ANSWER("error.question.answer.duplicate", 11007),
    QUESTION_TRUE_FALSE_INVALID("error.question.truefalse.invalid", 11008),
    INVALID_ANSWER("error.question.answer.invalid", 11101),
    ANSWER_NOT_FOUND("error.question.answer.not.found", 11102),
    TOO_MANY_ANSWERS("error.question.answer.too.many", 11103),
    QUESTION_NOT_BELONG_TO_QUIZ("error.question.not.belong.to.quiz", 11104),

    INVALID_QUIZ_ATTEMPT_DATA("error.quiz.attempt.invalid.data", 12001),
    QUIZ_ATTEMPT_EMPTY("error.quiz.attempt.empty", 12002),
    INVALID_QUIZ_ATTEMPT_STATE("error.quiz.attempt.invalid.state", 12003),
    QUIZ_ATTEMPT_ALREADY_DELETED("error.quiz.attempt.already.deleted", 12004),
    QUIZ_ATTEMPT_NOT_ALL_ANSWERED("error.quiz.attempt.not.all.answered", 12005),
    INVALID_USER_ANSWER("error.user.answer.invalid", 12101),
    INVALID_USER_ANSWER_DATA("error.user.answer.invalid.data", 12102),

    ;

    private final String messageKey;
    private final int errorCode;

    ErrorCode(String messageKey, int errorCode) {
        this.messageKey = messageKey;
        this.errorCode = errorCode;
    }

}
