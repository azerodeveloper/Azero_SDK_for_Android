package com.azero.sampleapp.impl.contactuploader;

import com.azero.platforms.contactuploader.ContactUploader;
import com.azero.sdk.AzeroManager;
import com.azero.sdk.impl.ContactIngestion.ContactUploader.AbstractContactUploaderDispatcher;

public class ContactUploaderHandler {

    private com.azero.sdk.impl.ContactIngestion.ContactUploader.ContactUploaderHandler mContactUploaderHandler;

    public ContactUploaderHandler() {
        if (mContactUploaderHandler == null) {
            mContactUploaderHandler = (com.azero.sdk.impl.ContactIngestion.ContactUploader.ContactUploaderHandler) AzeroManager.getInstance().getHandler(AzeroManager.CONTACT_UPLOADER_HANDLER);
        }
        registerListener();
    }

    private void registerListener() {
        mContactUploaderHandler.registerContactUploaderStatusChangedListener(new AbstractContactUploaderDispatcher() {
            @Override
            public void contactsUploaderStatusChanged(ContactUploader.ContactUploadStatus contactUploadStatus, String info) {
            }
        });
    }
}
