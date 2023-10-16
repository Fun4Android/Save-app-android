package net.opendasharchive.openarchive.features.media

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.esafirm.imagepicker.features.ImagePickerLauncher
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityPreviewBinding
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.db.Project
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.media.batch.BatchReviewMediaActivity
import net.opendasharchive.openarchive.features.media.review.ReviewMediaActivity
import net.opendasharchive.openarchive.util.AlertHelper
import net.opendasharchive.openarchive.util.Prefs
import net.opendasharchive.openarchive.util.extensions.hide
import net.opendasharchive.openarchive.util.extensions.show
import net.opendasharchive.openarchive.util.extensions.toggle

class PreviewActivity: BaseActivity(), View.OnClickListener, PreviewAdapter.Listener {

    companion object {
        const val PROJECT_ID_EXTRA = "project_id"
    }

    private lateinit var mBinding: ActivityPreviewBinding
    private lateinit var mPickerLauncher: ImagePickerLauncher

    private var mProject: Project? = null

    private val mAdapter: PreviewAdapter?
        get() = mBinding.mediaGrid.adapter as? PreviewAdapter

    private var mMedia: List<Media>
        get() = mAdapter?.currentList ?: emptyList()
        set(value) {
            mAdapter?.submitList(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mProject = Project.getById(intent.getLongExtra(PROJECT_ID_EXTRA, -1))

        mPickerLauncher = MediaPicker.register(this, mBinding.root, { mProject }, {
            refresh()
        })

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.title = getString(R.string.preview_media)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mBinding.mediaGrid.layoutManager = GridLayoutManager(this, 2)
        mBinding.mediaGrid.adapter = PreviewAdapter(this)
        mBinding.mediaGrid.setHasFixedSize(true)

        mBinding.btAddMore.setOnClickListener(this)
        mBinding.btBatchEdit.setOnClickListener(this)
        mBinding.btSelectAll.setOnClickListener(this)
        mBinding.btRemove.setOnClickListener(this)

        mediaSelectionChanged()

        refresh()
    }

    override fun onResume() {
        super.onResume()

        showFirstTimeBatch()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_preview, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()

                return true
            }
            R.id.menu_upload -> {
                mMedia.forEach {
                    it.sStatus = Media.Status.Queued
                    it.save()
                }

                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when (view) {
            mBinding.btAddMore -> {
                MediaPicker.pick(this, mPickerLauncher)
            }
            mBinding.btBatchEdit -> {
                val i = Intent(this, BatchReviewMediaActivity::class.java)
                i.putExtra(ReviewMediaActivity.EXTRA_CURRENT_MEDIA_ID,
                    mMedia.filter { it.selected }.map { it.id }.toLongArray())

                startActivity(i)
            }
            mBinding.btSelectAll -> {
                val select = mMedia.firstOrNull { !it.selected } != null

                mMedia.forEach {
                    if (it.selected != select) {
                        it.selected = select
                        it.save()

                        mAdapter?.notifyItemChanged(mMedia.indexOf(it))
                    }
                }

                mediaSelectionChanged()
            }
            mBinding.btRemove -> {
                mMedia.forEach {
                    if (it.selected) {
                        it.delete()
                    }
                }

                refresh()
                mediaSelectionChanged()
            }
        }
    }

    override fun mediaClicked(media: Media) {
        val i = Intent(this, ReviewMediaActivity::class.java)
        i.putExtra(ReviewMediaActivity.EXTRA_CURRENT_MEDIA_ID, media.id)

        startActivity(i)
    }

    override fun mediaSelectionChanged() {
        if (mMedia.firstOrNull { it.selected } != null) {
            mBinding.btAddMore.hide()
            mBinding.bottomBar.show()
        }
        else {
            mBinding.btAddMore.toggle(mProject != null)
            mBinding.bottomBar.hide()
        }
    }

    private fun refresh() {
        mMedia = Media.getByStatus(listOf(Media.Status.Local), Media.ORDER_CREATED)
    }

    private fun showFirstTimeBatch() {
        if (Prefs.batchHintShown) return

        AlertHelper.show(this, R.string.popup_batch_desc, R.string.popup_batch_title)

        Prefs.batchHintShown = true
    }
}