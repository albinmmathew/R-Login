package com.example.rlogin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AssignmentItem {
    public static final int STATE_UPCOMING = 1;
    public static final int STATE_DUE_SOON = 2;
    public static final int STATE_URGENT = 3;
    public static final int STATE_OVERDUE = 4;
    public static final int STATE_COMPLETED = 5;
    public static final int STATE_NO_DEADLINE = 6;

    private final String id;
    private final String title;
    private final String courseName;
    private final long dueTimestamp; // In seconds
    private final String formattedDueDate;
    private final String submissionUrl;
    private final String activityType; // "assign", "quiz", etc.
    private boolean isSubmitted;
    private boolean isManuallyClosed;

    public AssignmentItem(String id, String title, String courseName, long dueTimestamp,
                          String formattedDueDate, String submissionUrl, String activityType,
                          boolean isSubmitted, boolean isManuallyClosed) {
        this.id = id;
        this.title = title;
        this.courseName = courseName;
        this.dueTimestamp = dueTimestamp;
        this.formattedDueDate = formattedDueDate;
        this.submissionUrl = submissionUrl;
        this.activityType = activityType != null ? activityType : "assign";
        this.isSubmitted = isSubmitted;
        this.isManuallyClosed = isManuallyClosed;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCourseName() {
        return courseName;
    }

    public long getDueTimestamp() {
        return dueTimestamp;
    }

    public String getFormattedDueDate() {
        if (dueTimestamp <= 0) {
            return "No set deadline (submit before exam)";
        }
        if (formattedDueDate != null && !formattedDueDate.isEmpty()) {
            return formattedDueDate;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(dueTimestamp * 1000L));
    }

    public String getSubmissionUrl() {
        return submissionUrl;
    }

    public String getActivityType() {
        return activityType;
    }

    public boolean isSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }

    public boolean isManuallyClosed() {
        return isManuallyClosed;
    }

    public void setManuallyClosed(boolean manuallyClosed) {
        isManuallyClosed = manuallyClosed;
    }

    public boolean isClosed() {
        return isSubmitted || isManuallyClosed;
    }

    public int getState() {
        if (isClosed()) {
            return STATE_COMPLETED;
        }

        long now = System.currentTimeMillis();
        long dueMillis = dueTimestamp * 1000L;

        if (dueMillis <= 0) {
            return STATE_NO_DEADLINE;
        }

        if (dueMillis < now) {
            return STATE_OVERDUE;
        }

        long diff = dueMillis - now;
        if (diff <= 24L * 3600L * 1000L) {
            return STATE_URGENT;
        } else if (diff <= 7L * 24L * 3600L * 1000L) {
            return STATE_DUE_SOON;
        } else {
            return STATE_UPCOMING;
        }
    }

    public String getCountdownText() {
        if (isSubmitted) {
            return "Submitted on Moodle ✓";
        }
        if (isManuallyClosed) {
            return "Marked as Done ✓";
        }

        long now = System.currentTimeMillis();
        long dueMillis = dueTimestamp * 1000L;

        if (dueMillis <= 0) {
            return "No Set Deadline";
        }

        if (dueMillis < now) {
            long overdueDiff = now - dueMillis;
            long hours = overdueDiff / (3600L * 1000L);
            long days = hours / 24L;
            if (days > 0) {
                return "Overdue by " + days + (days == 1 ? " day" : " days");
            } else if (hours > 0) {
                return "Overdue by " + hours + (hours == 1 ? " hour" : " hours");
            } else {
                long minutes = overdueDiff / (60L * 1000L);
                return "Overdue by " + Math.max(1, minutes) + " mins";
            }
        }

        long diff = dueMillis - now;
        long hours = diff / (3600L * 1000L);
        long days = hours / 24L;

        if (days >= 2) {
            return "Due in " + days + " days";
        } else if (days == 1) {
            return "Due tomorrow";
        } else if (hours > 0) {
            return "Due in " + hours + (hours == 1 ? " hour" : " hours");
        } else {
            long minutes = diff / (60L * 1000L);
            return "Due in " + Math.max(1, minutes) + " mins";
        }
    }
}
