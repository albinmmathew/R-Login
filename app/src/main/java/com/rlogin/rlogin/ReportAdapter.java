package com.rlogin.rlogin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private final List<SubjectItem> items;
    private boolean showDutyLeave = true; // Default: With OD first!

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
        boolean isBrutalist = ThemeManager.isBrutalist(parent.getContext());
        int layoutId = isBrutalist ? R.layout.item_subject_brutalist : R.layout.item_subject;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubjectItem item = items.get(position);
        holder.txtName.setText(item.name);
        holder.txtCode.setText(item.code);

        String pctVal;
        if (showDutyLeave) {
            holder.txtHeld.setText(item.dutyTotalHeld > 0 ? String.valueOf(item.dutyTotalHeld) : (item.totalHeld > 0 ? String.valueOf(item.totalHeld) : "-"));
            holder.txtAttended.setText(String.valueOf((int) item.dutyAttended));
            pctVal = item.dutyPct.isEmpty() ? "-" : (item.dutyPct.contains("%") ? item.dutyPct : item.dutyPct + "%");
        } else {
            holder.txtHeld.setText(item.totalHeld > 0 ? String.valueOf(item.totalHeld) : "-");
            holder.txtAttended.setText(String.valueOf((int) item.attended));
            pctVal = item.pct.isEmpty() ? "-" : (item.pct.contains("%") ? item.pct : item.pct + "%");
        }
        holder.txtPct.setText(pctVal);

        boolean isBrutalist = ThemeManager.isBrutalist(holder.itemView.getContext());
        String pctStr = pctVal.replace("%", "").trim();
        try {
            double p = Double.parseDouble(pctStr);
            if (isBrutalist) {
                holder.txtPct.setTextColor(p >= 75.0 ? 0xFF11A677 : 0xFFD52C36);
            } else {
                holder.txtPct.setTextColor(p >= 75.0 ? 0xFF4CAF50 : 0xFFF44336);
            }
        } catch (Exception e) {
            holder.txtPct.setTextColor(isBrutalist ? 0xFFCD5535 : 0xFF002868);
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
