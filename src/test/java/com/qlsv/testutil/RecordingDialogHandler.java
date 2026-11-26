package com.qlsv.testutil;

import com.qlsv.controller.DialogHandler;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Hộp thoại giả cho test Controller: ghi lại thông báo thay vì bật cửa sổ,
 * và trả lời xác nhận theo {@link #confirmResponse}.
 */
public class RecordingDialogHandler implements DialogHandler {

    public String lastMessage;
    public String lastTitle;
    public int lastMessageType;
    public int confirmResponse = JOptionPane.YES_OPTION;
    public final List<String> messages = new ArrayList<>();

    @Override
    public void showMessage(Component parent, Object message, String title, int messageType) {
        lastMessage = String.valueOf(message);
        lastTitle = title;
        lastMessageType = messageType;
        messages.add(lastMessage);
    }

    @Override
    public int showConfirm(Component parent, Object message, String title, int optionType) {
        lastMessage = String.valueOf(message);
        lastTitle = title;
        return confirmResponse;
    }
}
