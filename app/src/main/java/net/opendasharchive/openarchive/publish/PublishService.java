package net.opendasharchive.openarchive.publish;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import net.opendasharchive.openarchive.publish.model.Job;
import net.opendasharchive.openarchive.publish.model.PublishJob;

public class PublishService extends IntentService implements PublishController.PublishListener {
    public static final String TAG = "PublishService";
    public static final String INTENT_EXTRA_PUBLISH_URL = "publish_url";
    public static final String INTENT_EXTRA_PUBLISH_JOB_ID = "publish_job_id";
    public static final String INTENT_EXTRA_JOB_ID = "job_id";
    public static final String INTENT_EXTRA_ERROR_CODE = "error_code";
    public static final String INTENT_EXTRA_ERROR_MESSAGE = "error_message";
    public static final String INTENT_EXTRA_SITE_KEYS = "site_keys";
    public static final String INTENT_EXTRA_PROGRESS = "progress_percent";
    public static final String INTENT_EXTRA_PROGRESS_MESSAGE = "progress_message";
    public static final String ACTION_RENDER = "net.opendasharchive.openarchive.publish.action.RENDER";
    public static final String ACTION_UPLOAD = "net.opendasharchive.openarchive.publish.action.UPLOAD";
    public static final String ACTION_PUBLISH_SUCCESS = "net.opendasharchive.openarchive.publish.action.PUBLISH_SUCCESS";
    public static final String ACTION_PUBLISH_FAILURE = "net.opendasharchive.openarchive.publish.action.PUBLISH_FAILURE";
    public static final String ACTION_JOB_SUCCESS = "net.opendasharchive.openarchive.publish.action.JOB_SUCCESS";
    public static final String ACTION_JOB_FAILURE = "net.opendasharchive.openarchive.publish.action.JOB_FAILURE";
    public static final String ACTION_PROGRESS = "net.opendasharchive.openarchive.publish.action.PROGRESS";
    
    public PublishService() {
        super("PublishService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra(INTENT_EXTRA_PUBLISH_JOB_ID)) {
            int id = intent.getIntExtra(INTENT_EXTRA_PUBLISH_JOB_ID, -1);
            if (id != -1) {
                // TODO need to trigger controllers for all selected sites
                PublishController controller = (new PublishController(getApplicationContext(), this));
                 if (intent.getAction().equals(ACTION_UPLOAD)) {
                    controller.startUpload(id);
                }
            } else {
                Log.d(TAG, "invalid publishJobId passed: " + id);
            }
        }
    }

    @Override
    public void publishSucceeded(PublishJob publishJob, String url) {
        Intent intent = new Intent(ACTION_PUBLISH_SUCCESS);
        intent.putExtra(INTENT_EXTRA_PUBLISH_URL, url);
        intent.putExtra(INTENT_EXTRA_PUBLISH_JOB_ID, publishJob.getId());
        sendBroadcast(intent);
    }

    @Override
    public void publishFailed(PublishJob publishJob, int errorCode, String errorMessage) {
        Intent intent = new Intent(ACTION_PUBLISH_FAILURE);
        intent.putExtra(INTENT_EXTRA_PUBLISH_JOB_ID, publishJob.getId());
        intent.putExtra(INTENT_EXTRA_ERROR_CODE, errorCode);
        intent.putExtra(INTENT_EXTRA_ERROR_MESSAGE, errorMessage);
        sendBroadcast(intent);
    }

    @Override
    public void publishProgress(PublishJob publishJob, float progress, String message) {
        Intent intent = new Intent(ACTION_PROGRESS);
        intent.putExtra(INTENT_EXTRA_PUBLISH_JOB_ID, publishJob.getId());
        intent.putExtra(INTENT_EXTRA_PROGRESS, progress);
        intent.putExtra(INTENT_EXTRA_PROGRESS_MESSAGE, message);
        sendBroadcast(intent);
    }

    @Override
    public void jobSucceeded(Job job) {
        Intent intent = new Intent(ACTION_JOB_SUCCESS);
        intent.putExtra(INTENT_EXTRA_PUBLISH_JOB_ID, job.getPublishJobId());
        intent.putExtra(INTENT_EXTRA_JOB_ID, job.getId());
        sendBroadcast(intent);
    }

    @Override
    public void jobFailed(Job job, int errorCode, String errorMessage) {
        Intent intent = new Intent(ACTION_JOB_FAILURE);
        intent.putExtra(INTENT_EXTRA_PUBLISH_JOB_ID, job.getPublishJobId());
        intent.putExtra(INTENT_EXTRA_JOB_ID, job.getId());
        sendBroadcast(intent);
    }
}
