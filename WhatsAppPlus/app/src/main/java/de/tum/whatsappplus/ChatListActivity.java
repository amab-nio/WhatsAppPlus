package de.tum.whatsappplus;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.logging.Logger;

public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = ChatListActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        TableLayout table = (TableLayout) findViewById(R.id.chat_list_table);
        if (table != null) {
            table.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

            for (String key : Constants.contacts.keySet()) {
                Contact c = Constants.contacts.get(key);
                if (c.chat == null) continue;

                View chat1 = getLayoutInflater().inflate(R.layout.view_chat_history_item, table, false);
                ((ImageView) chat1.findViewById(R.id.chat_icon)).setImageResource(c.imageID);
                ((TextView) chat1.findViewById(R.id.chat_name)).setText(c.name);
                ((TextView) chat1.findViewById(R.id.chat_history_last)).setText(c.chat.get(c.chat.size()-1).text);
                ((TextView) chat1.findViewById(R.id.chat_timestamp)).setText(c.chat.get(c.chat.size()-1).timeStamp);
                chat1.setTag(R.string.tag_chat_id, c.name);

                table.addView(chat1);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        TableLayout table = (TableLayout) findViewById(R.id.chat_list_table);
        for (int i = 0; i < table.getChildCount(); i++) {
            View chat1 = table.getChildAt(i);
            String chatId = (String) chat1.getTag(R.string.tag_chat_id);
            Contact contact = Constants.contacts.get(chatId);
            ((TextView) chat1.findViewById(R.id.chat_history_last)).setText(contact.chat.get(contact.chat.size()-1).text);
            ((TextView) chat1.findViewById(R.id.chat_timestamp)).setText(contact.chat.get(contact.chat.size()-1).timeStamp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat_list, menu);
        return true;
    }

    public void onChatSummaryClick(View view) {
        Log.i(TAG, "clicked on " + view.getTag(R.string.tag_chat_id));
        Intent openChat = new Intent(this, ChatActivity.class);
        openChat.putExtra(Constants.EXTRA_CHAT_ID, (String) view.getTag(R.string.tag_chat_id));
        startActivity(openChat);
    }

}
