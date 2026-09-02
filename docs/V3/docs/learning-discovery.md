# Learning Discovery

## 1. Purpose

V3 begins by exploring how a German-learning product can organize the learning experience.

DeutschTiger is used as a reference to observe different forms of learning, learning activities, and ways of representing the learner's state.

The purpose of this exploration is not to copy DeutschTiger or use it as a specification for DeutschHub.

The observations from DeutschTiger are used only to broaden the perspective and raise questions that can later be investigated within DeutschHub's Learning Context.

---

## 2. Observed Learning Experience

Based on the observed product experience, DeutschTiger's learning experience can be viewed from four main aspects.

### 2.1. Learning Structure

DeutschTiger organizes learning through multiple structures rather than only through Courses.

Observed structures include:

* Course-based learning;
* Vocabulary;
* Grammar;
* Language Skills;
* learning content organized by level and topic;
* specialized learning areas.

Course remains an important structure, but it is not the only way learners access learning.

**Observation:**

> A learning experience can be organized through multiple structures, with a Course being one possible form of organization.

---

### 2.2. Learning Activities

DeutschTiger provides different types of activities throughout the learning process, including:

* learning new content;
* practice and exercises;
* review;
* listening;
* speaking;
* reading;
* writing;
* learning through video and authentic content.

These activities can appear in different learning contexts.

For example, content can be used for watching, reading, vocabulary lookup, practice, or review.

**Observation:**

> Learning is not limited to completing a unit of content. A learning experience can consist of multiple types of learning activities.

---

### 2.3. Learner State

DeutschTiger represents the learner's learning state through multiple aspects, including:

* learning progress;
* competency;
* vocabulary learned;
* grammar progress;
* XP;
* level;
* streak;
* achievements;
* learning statistics.

Progress is therefore not represented only through the completion of a Course.

**Observation:**

> A learner's state can represent multiple aspects of the learning process rather than only content completion.

---

### 2.4. Learning Direction

DeutschTiger does not only provide content for learners to select themselves.

The product also presents mechanisms that help direct learning, including:

* daily learning;
* learning plans;
* recommended activities;
* content due for review;
* weakness-oriented practice;
* learning goals or exam-oriented preparation;
* exam preparation.

This creates an experience in which the system can help the learner determine what to do next.

**Observation:**

> A learning experience can include guidance toward the next activity based on the learner's goals and current state.

---

## 3. Initial Observation

Based on the observations above, the learning experience can be temporarily viewed through four aspects:

```text
Learning Experience
│
├── Learning Structure
│   ├── Course
│   ├── Vocabulary
│   ├── Grammar
│   └── Skills
│
├── Learning Activities
│   ├── Learning
│   ├── Practice
│   ├── Review
│   └── Skill-based activities
│
├── Learner State
│   ├── Progress
│   ├── Competency
│   ├── Achievement
│   └── Learning statistics
│
└── Learning Direction
    ├── Daily Learning
    ├── Learning Plan
    ├── Recommendations
    └── Exam Preparation
```

This is **not the domain model of DeutschHub**.

It is only a way of organizing the observations from the DeutschTiger exploration.

No conclusion is made at this stage that any of these elements should become:

* an Entity;
* an Aggregate;
* a Value Object;
* a Module;
* a Bounded Context;
* or an independent feature in DeutschHub.

---

## 4. Initial Questions for DeutschHub

The observations above lead to several questions that need to be investigated in V3.

### Course

* What role does a Course play in the overall Learning experience?
* Does learning necessarily take place through a Course?
* Is a Course the central structure of Learning or one way of organizing learning content?

### Learning Activities

* What is a learning activity in DeutschHub?
* Can a learning activity exist outside a Course?
* Are Practice and Review simply different states of content completion, or do they have their own responsibilities?

### Progress

* What does Progress actually represent?
* Does Progress only represent content completion?
* How should learner progress be represented beyond an individual Course?

### Vocabulary and Grammar

* Are Vocabulary and Grammar simply types of learning content?
* Or do they have their own learning activities and responsibilities?
* Can they be learned independently from a Course?

### Learning Direction

* Is a learning plan part of the responsibility of Learning?
* Should the system determine or recommend the learner's next activity?
* Do learning goals affect how the system organizes the learning experience?

---

## 5. Conclusion

The DeutschTiger exploration provides a broader view of the learning experience:

> **Learning is not necessarily equivalent to Course completion.**

A learning experience can involve:

```text
Structure
    +
Activities
    +
Learner State
    +
Learning Direction
```

For DeutschHub, these observations do not define a target model.

They only indicate that the current implementation should be examined through a broader question:

> **What does the Learning Context of DeutschHub actually represent?**

The answer to this question will be determined in the following analysis stages, based on DeutschHub's own domain and current implementation.
