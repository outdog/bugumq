# 一对多文件广播 #

一对多文件广播，是基于消息广播Topic实现的，使用的原理是相同的，只不过传输的是二进制数据。

## 方法说明 ##

Client类提供了以下方法，用于一对多文件广播：
```
//设置文件广播监听器
public void setFileBroadcastListener(FileBroadcastListener broadcastListener)

//订阅文件广播
public void subscribeFileBroadcast(String... topics)

//退订文件广播
public void unsubscribeFileBroadcast(String... topics)

//开始文件广播。注意，这里返回一个long型的fileId
public long startBroadcastFile(String topic, Map<String, String> extras)

//广播文件数据
public void broadcastFileData(String topic, long fileId, byte[] data)

//结束文件广播
public void endBroadcastFile(String topic, long fileId)
```
其中，FileBroadcastListener是一个监听器类，它是一个抽象类，包含如下几个抽象方法：
```
//接收到文件广播开始消息
public abstract void onFileStart(String topic, long fileId, Map<String,String> extras);

//接收到文件广播结束消息
public abstract void onFileEnd(String topic, long fileId);

//接收到文件广播数据    
public abstract void onFileData(String topic, long fileId, byte[] data);
```

## 示例代码 ##

广播发布方：
```
    public void broadcastSend() throws Exception {
        Client client = Connection.getInstance().getClient();
        String topic = "video_broadcast";
        //开始广播文件
        long fileId = client.startBroadcastFile(topic, null);
        String filePath = "/Users/frankwen/send/redis.pdf";
        FileInputStream fis = new FileInputStream(filePath);
        int chunkSize = 16 * 1024;
        byte[] chunkData = new byte[chunkSize];
        int bytesRead = fis.read(chunkData, 0, chunkSize);
        //分块发送文件数据
        while(bytesRead > 0){
            if(bytesRead == chunkSize){
                client.broadcastFileData(topic, fileId, chunkData);
            }else{
                client.broadcastFileData(topic, fileId, Arrays.copyOfRange(chunkData, 0, bytesRead));
            }
            bytesRead = fis.read(chunkData, 0, chunkSize);
        }
        //结束广播文件
        client.endBroadcastFile(topic, fileId);
    }
```

广播接收方：
```
    public void broadcastReceive() throws Exception {
        Client client = Connection.getInstance().getClient();
        //设置文件广播监听器
        client.setFileBroadcastListener(new MyFileBroadcastListener());
        //订阅某个topic上的文件广播
        client.subscribeFileBroadcast("video_broadcast");
    }
    
    class MyFileBroadcastListener extends FileBroadcastListener{
        
        private FileOutputStream fos;
        
        @Override
        public void onFileStart(String topic, long fileId, Map<String, String> extras) {
            System.out.println("开始接收广播文件");
            try{
                File file = new File("/Users/frankwen/receive/new_file.pdf");
                file.createNewFile();
                fos = new FileOutputStream(file);
            }catch(IOException ex){
                
            }
        }

        @Override
        public void onFileEnd(String topic, long fileId) {
            try{
                fos.flush();
                fos.close();
            }catch(IOException ex){
                
            }
            System.out.println("广播文件接收完成");
        }

        @Override
        public void onFileData(String topic, long fileId, byte[] data) {
            try{
                fos.write(data);
            }catch(IOException ex){
                
            }
        }
        
    }
```

**注意：**

1、当有多个文件同时在传输的时候，需要用fileId进行区分。fileId是由BuguMQ自动产生的、能唯一区分不同文件的long型数值。

2、鉴于消息广播的不可靠性，一对多文件广播，只适用于小文件的传输，或者对传输可靠性要求不高的场合。

3、BuguMQ按块进行文件传输，块的大小由开发者决定。推荐值是16K字节。