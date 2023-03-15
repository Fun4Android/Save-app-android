package net.opendasharchive.openarchive.publish

import android.content.Context
import android.content.Intent
import android.os.Message
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.opendasharchive.openarchive.MainActivity
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.services.SiteController
import net.opendasharchive.openarchive.services.SiteControllerListener
import timber.log.Timber

class UploaderListenerV2(
    private val uploadMedia: Media,
    private val context: Context
) : SiteControllerListener {

    override fun success(msg: Message?) {
        uploadMedia.progress = uploadMedia.contentLength
        notifyMediaUpdated(uploadMedia)
        uploadMedia.status = Media.STATUS_UPLOADED
        uploadMedia.save()
        notifyMediaUpdated(uploadMedia)
    }

    override fun progress(msg: Message?) {
        val data = msg?.data
        val contentLengthUploaded = data?.getLong(SiteController.MESSAGE_KEY_PROGRESS)
        uploadMedia.progress = contentLengthUploaded ?: 0
        notifyMediaUpdated(uploadMedia)
    }

    override fun failure(msg: Message?) {
        val data = msg!!.data
        val errorCode = data.getInt(SiteController.MESSAGE_KEY_CODE)
        val errorMessage = data.getString(SiteController.MESSAGE_KEY_MESSAGE)
        val error = "Error $errorCode: $errorMessage"

        Timber.d("upload error: $error")

        uploadMedia.statusMessage = error
        uploadMedia.status = Media.STATUS_ERROR
        uploadMedia.save()

        notifyMediaUpdated(uploadMedia)
    }

    // Send an Intent with an action named "custom-event-name". The Intent sent should
    // be received by the ReceiverActivity.
    private fun notifyMediaUpdated(media: Media) {
        Timber.tag("sender").d("Broadcasting message")
        val intent = Intent(MainActivity.INTENT_FILTER_NAME)
        intent.putExtra(SiteController.MESSAGE_KEY_MEDIA_ID, media.id)
        intent.putExtra(SiteController.MESSAGE_KEY_STATUS, media.status)
        intent.putExtra(SiteController.MESSAGE_KEY_PROGRESS, media.progress)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

}