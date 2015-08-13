package com.scalyr.s3bench;

import com.scalyr.s3bench.DataObject;
import com.scalyr.s3bench.RandomObjectQueue;
import com.scalyr.s3bench.TaskInfo;
import com.scalyr.s3bench.Timer;

import java.io.DataInputStream;

import org.apache.logging.log4j.Logger;

class ReadTask implements Runnable
{
    private TaskInfo taskInfo;
    private RandomObjectQueue objectQueue;
    private DataObject dataObject;
    private int successfulOperations;
    private int errorCount;

    public ReadTask( TaskInfo taskInfo, RandomObjectQueue objectQueue )
    {
        this.taskInfo = taskInfo;
        this.objectQueue = objectQueue;
        this.dataObject = null;
    }

    public void prepare()
    {
        if ( this.dataObject == null || this.dataObject.size() != taskInfo.objectSize )
        {
            this.dataObject = new DataObject( taskInfo.objectSize );
        }
        else
        {
            this.dataObject.clear();
        }
    }

    public int successfulOperations()
    {
        return this.successfulOperations;
    }

    public int errorCount()
    {
        return this.errorCount;
    }

    public void run()
    {
        this.successfulOperations = 0;
        this.errorCount = 0;
        Timer timer = new Timer();
        String objectName = this.objectQueue.nextObject();
        while ( objectName != null )
        {
            prepare();

            timer.start();

            String error = this.dataObject.read( this.taskInfo, objectName );

            timer.stop();

            if ( error == null )
            {
                error = this.dataObject.verifyData( this.taskInfo.bucketName, objectName );
            }

            //test again because it might have been set by the call to verifyData
            if ( error == null )
            {
                ++this.successfulOperations;
            }
            else
            {
                ++this.errorCount;
            }

            this.taskInfo.logResult( objectName, timer.elapsedMilliseconds(), error );

            objectName = this.objectQueue.nextObject();

        }
    }
}

