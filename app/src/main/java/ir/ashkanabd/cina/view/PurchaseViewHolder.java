package ir.ashkanabd.cina.view;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.rey.material.widget.Button;
import com.rey.material.widget.RelativeLayout;
import ir.ashkanabd.cina.R;

public class PurchaseViewHolder extends RecyclerView.ViewHolder {

    private TextView itemName;
    private Button itemPrice;
    private Button itemButton;

    public PurchaseViewHolder(@NonNull View itemView) {
        super(itemView);
        RelativeLayout fileLayout = (RelativeLayout) itemView;
        itemName = fileLayout.findViewById(R.id.name_purchase_item);
        itemPrice = fileLayout.findViewById(R.id.price_purchase_item);
        itemButton = fileLayout.findViewById(R.id.purchase_purchase_item);
    }

    public TextView getItemName() {
        return itemName;
    }

    public TextView getItemPrice() {
        return itemPrice;
    }

    public Button getItemButton() {
        return itemButton;
    }
}
