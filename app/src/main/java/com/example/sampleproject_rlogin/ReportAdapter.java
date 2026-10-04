package com.example.sampleproject_rlogin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private final List<SubjectItem> items;
    private boolean showDutyLeave = false;

    public ReportAdapter(List<SubjectItem> items) {
        this.items = items;
    }

    public void setShowDutyLeave(boolean showDutyLeave) {
        this.showDutyLeave = showDutyLeave;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubjectItem item = items.get(position);
        holder.txtName.setText(item.name);
        holder.txtCode.setText(item.code);

        if (showDutyLeave) {
            holder.txtHeld.setText(item.dutyTotalHeld > 0 ? String.valueOf(item.dutyTotalHeld) : (item.totalHeld > 0 ? String.valueOf(item.totalHeld) : "-"));
            holder.txtAttended.setText(String.valueOf((int) item.dutyAttended));
            holder.txtPct.setText(item.dutyPct.isEmpty() ? "-" : (item.dutyPct.contains("%") ? item.dutyPct : item.dutyPct + "%"));
        } else {
            holder.txtHeld.setText(item.totalHeld > 0 ? String.valueOf(item.totalHeld) : "-");
            holder.txtAttended.setText(String.valueOf((int) item.attended));
            holder.txtPct.setText(item.pct.isEmpty() ? "-" : (item.pct.contains("%") ? item.pct : item.pct + "%"));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtCode, txtHeld, txtAttended, txtPct;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            txtCode = itemView.findViewById(R.id.txt_code);
            txtHeld = itemView.findViewById(R.id.txt_held);
            txtAttended = itemView.findViewById(R.id.txt_attended);
            txtPct = itemView.findViewById(R.id.txt_pct);
        }
    }
}
