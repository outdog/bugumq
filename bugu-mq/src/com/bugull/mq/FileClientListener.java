/*
 * Copyright (c) www.bugull.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bugull.mq;

/**
 * A QueueListener used for transfer files.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class FileClientListener extends QueueListener {
    
    private FileListener fileListener;
    
    public FileClientListener(FileListener fileListener){
        this.fileListener = fileListener;
    }
    
    @Override
    public void onQueueMessage(String queue, String message) {
        if(StringUtil.isEmpty(message)){
            return;
        }
        FileMessage fm = FileMessage.parse(message);
        String fromClientId = fm.getFromClientId();
        long fileId = fm.getFileId();
        String filePath = fm.getFilePath();
        long fileLength = fm.getFileLength();
        int type = fm.getType();
        switch(type){
            case MQ.FILE_REQUEST:
                fileListener.onRequest(fromClientId, fileId, filePath, fileLength);
                break;
            case MQ.FILE_AGREE:
                fileListener.onAgree(fromClientId, fileId, filePath, fileLength);
                break;
            case MQ.FILE_REJECT:
                fileListener.onReject(fromClientId, fileId, filePath, fileLength);
                break;
            default:
                break;
        }
    }

}
