package de.tum.whatsappplus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.Calendar;

public class ChatActivity extends AppCompatActivity implements View.OnLongClickListener, View.OnClickListener {

    private static final String TAG = ChatActivity.class.getName();
    private static final int MESSAGE_MARGIN_TOP = 5;

    private EditText chatInputEditText;
    private TableLayout table;

    private Contact contact;
    private int selectedMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String chatId = getIntent().getStringExtra(Constants.EXTRA_CHAT_ID);

        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView toolbarIcon = (ImageView) toolbar.findViewById(R.id.toolbar_icon);
        toolbarIcon.setImageDrawable(getResources().getDrawable(Constants.contacts.get(chatId).imageID));
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(chatId);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        table = (TableLayout) findViewById(R.id.chat_table);
        table.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.v(TAG, "layout changed: " + left + " t=" + top + " r=" + right + " b=" + bottom + " oL=" + oldLeft + " oT=" + oldTop + " oR=" + oldRight + " oB=" + oldBottom);
                ScrollView scrollView = (ScrollView) findViewById(R.id.chat_scrollview);
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        if (table != null) {
            contact = Constants.contacts.get(chatId);
            getSupportActionBar().setTitle(contact.name);
            for (Message message : contact.chat) {
                addNewChatMessage(message);
            }
        }

        table.requestFocus();

        chatInputEditText = (EditText) findViewById(R.id.chat_input_edittext);
        if (chatInputEditText != null) {
            chatInputEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ImageButton sendButton = (ImageButton) findViewById(R.id.chat_input_voice_send);
                    if (sendButton != null)
                        if (s.length() > 0) {
                            sendButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_white_24dp));
                        } else {
                            sendButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_white_24dp));
                        }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void addNewChatMessage(Message message) {
        View chatItem = getLayoutInflater().inflate(R.layout.view_chat_item, table, false);
        ((TextView) chatItem.findViewById(R.id.chat_message)).setText(message.text);
        ((TextView) chatItem.findViewById(R.id.chat_timestamp)).setText(message.timeStamp);

        View chatMessageContent = chatItem.findViewById(R.id.chat_message_content);

        TableLayout.LayoutParams chatItemLayoutParams = (TableLayout.LayoutParams) chatItem.getLayoutParams();
        chatItemLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MESSAGE_MARGIN_TOP, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams chatMessageContentLayoutParams = (LinearLayout.LayoutParams) chatMessageContent.getLayoutParams();

        if (message.author.equals("self")) {
            chatMessageContentLayoutParams.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            chatMessageContentLayoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            chatMessageContent.setBackground(getResources().getDrawable(R.drawable.drawable_chat_item_background_self));
        } else {
            chatMessageContentLayoutParams.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            chatMessageContentLayoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            chatMessageContent.setBackground(getResources().getDrawable(R.drawable.drawable_chat_item_background_other));
        }

        chatItem.setTag(R.string.tag_chat_owner, message.author);
        chatItem.setTag(R.string.tag_selected, false);
        chatMessageContent.setOnLongClickListener(this);
        chatMessageContent.setOnClickListener(this);

        chatMessageContent.setLayoutParams(chatMessageContentLayoutParams);

        table.addView(chatItem, chatItemLayoutParams);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        return true;
    }

    public void onSendClick(View view) {
        Editable editable = chatInputEditText.getText();
        if (editable.length() > 0) {
            char[] messageTextChars = new char[editable.length()];
            editable.getChars(0, editable.length(), messageTextChars, 0);
            String messageText = new String(messageTextChars);

            Calendar cal = Calendar.getInstance();
            String timeStampString = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);

            Message message = new Message("self", messageText, timeStampString);

            addNewChatMessage(message);

            if (contact != null) {
                contact.chat.add(message);
            }

            editable.clear();
        }
    }

    public void onConvertToGroupClick(MenuItem menuItem) {
        Intent convertToGroupIntent = new Intent(this, GroupCreationActivity.class);
        convertToGroupIntent.putExtra(Constants.EXTRA_CHAT_ID, contact.name);
        startActivity(convertToGroupIntent);
    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "long click: " + v);

        selectOrDeselectMessage(v);
        return true;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "click: " + v);

        if (selectedMessages > 0) {
            selectOrDeselectMessage(v);
        }
    }

    private void selectOrDeselectMessage(View messageContent) {
        View chatItem = (View) messageContent.getParent();
        boolean chatItemSelected = (boolean) chatItem.getTag(R.string.tag_selected);
        if ("self".equals(chatItem.getTag(R.string.tag_chat_owner))) {
            selectSelfMessage(!chatItemSelected, messageContent, chatItem);
        } else {
            selectOtherMessage(!chatItemSelected, messageContent, chatItem);
        }
    }

    private void selectSelfMessage(boolean toggle, View messageContent, View chatItem) {
        if (toggle) {
            selectedMessages++;
            messageContent.setBackground(getResources().getDrawable(R.drawable.drawable_chat_item_background_self_sel));
            chatItem.setBackgroundColor(getResources().getColor(R.color.color_chat_item_background_sel));
            chatItem.setTag(R.string.tag_selected, true);
        } else {
            selectedMessages--;
            messageContent.setBackground(getResources().getDrawable(R.drawable.drawable_chat_item_background_self));
            chatItem.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            chatItem.setTag(R.string.tag_selected, false);
        }
    }

    private void selectOtherMessage(boolean toggle, View messageContent, View chatItem) {
        if (toggle) {
            selectedMessages++;
            messageContent.setBackground(getResources().getDrawable(R.drawable.drawable_chat_item_background_other_sel));
            chatItem.setBackgroundColor(getResources().getColor(R.color.color_chat_item_background_sel));
            chatItem.setTag(R.string.tag_selected, true);
        } else {
            selectedMessages--;
            messageContent.setBackground(getResources().getDrawable(R.drawable.drawable_chat_item_background_other));
            chatItem.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            chatItem.setTag(R.string.tag_selected, false);
        }
    }

    public void onBackButtonClick(View view) {
        finish();
    }

}
