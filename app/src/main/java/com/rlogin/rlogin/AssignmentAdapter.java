package com.rlogin.rlogin;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    public interface OnAssignmentActionListener {
        void onOpenAssignment(AssignmentItem item);
        void onToggleDone(AssignmentItem item);
    }

    private final List<AssignmentItem> items = new ArrayList<>();
    private final OnAssignmentActionListener listener;

    public AssignmentAdapter(List<AssignmentItem> initialItems, OnAssignmentActionListener listener) {
        if (initialItems != null) {
            this.items.addAll(initialItems);
        }
        this.listener = listener;
    }

    public void updateList(List<AssignmentItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignmentItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView badgeUrgency;
        private final TextView txtActivityType;
        private final TextView txtTitle;
        private final TextView txtCourse;
        private final TextView txtDueDate;
        private final Button btnOpen;
        private final Button btnToggleDone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeUrgency = itemView.findViewById(R.id.badge_urgency);
            txtActivityType = itemView.findViewById(R.id.txt_activity_type);
            txtTitle = itemView.findViewById(R.id.txt_assignment_title);
            txtCourse = itemView.findViewById(R.id.txt_course_name);
            txtDueDate = itemView.findViewById(R.id.txt_due_date);
            btnOpen = itemView.findViewById(R.id.btn_open_assignment);
            btnToggleDone = itemView.findViewById(R.id.btn_toggle_done);
        }

        public void bind(AssignmentItem item, OnAssignmentActionListener listener) {
            txtTitle.setText(item.getTitle());
            txtCourse.setText(item.getCourseName());
            txtDueDate.setText("Due: " + item.getFormattedDueDate());

            String type = item.getActivityType();
            if (type != null && type.equalsIgnoreCase("quiz")) {
                txtActivityType.setText("QUIZ");
            } else {
                txtActivityType.setText("ASSIGNMENT");
            }

            badgeUrgency.setText(item.getCountdownText());

            int state = item.getState();
            int badgeColor;
            switch (state) {
                case AssignmentItem.STATE_OVERDUE:
                    badgeColor = 0xFFD32F2F; // Red
                    break;
                case AssignmentItem.STATE_URGENT:
                    badgeColor = 0xFFE64A19; // Deep Orange / Red-Orange
                    break;
                case AssignmentItem.STATE_DUE_SOON:
                    badgeColor = 0xFFF57C00; // Orange / Amber
                    break;
                case AssignmentItem.STATE_COMPLETED:
                    badgeColor = 0xFF546E7A; // Slate Grey
                    break;
                case AssignmentItem.STATE_NO_DEADLINE:
                    badgeColor = 0xFF0277BD; // Ocean Blue (Undated)
                    break;
                case AssignmentItem.STATE_UPCOMING:
                default:
                    badgeColor = 0xFF2E7D32; // Green
                    break;
            }

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(16f);
            shape.setColor(badgeColor);
            badgeUrgency.setBackground(shape);

            if (item.isClosed()) {
                btnToggleDone.setText("Restore ↩");
            } else {
                btnToggleDone.setText("Mark Done ✓");
            }

            btnOpen.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpenAssignment(item);
                }
            });

            btnToggleDone.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleDone(item);
                }
            });
        }
    }
}
