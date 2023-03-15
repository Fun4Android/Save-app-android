package net.opendasharchive.openarchive.db

import com.orm.SugarRecord
import java.util.Date

data class Project(
    var description: String? = null,
    var created: Date? = null,
    var spaceId: Long? = null,
    var archived: Boolean = false,
    var openCollectionId: Long = -1,
    var licenseUrl: String? = null
) : SugarRecord() {

    companion object {

        fun getAllBySpace(spaceId: Long): List<Project>? {
            val whereArgs = arrayOf(spaceId.toString() + "")
            return find(Project::class.java, "space_id = ?", whereArgs, "", "ID DESC", "")
        }

        fun getAllBySpace(spaceId: Long, archived: Boolean): List<Project>? {
            val isArchived = if (archived) 1 else 0
            val whereArgs = arrayOf(spaceId.toString() + "", isArchived.toString() + "")
            return find(Project::class.java, "space_id = ? AND archived = ?", whereArgs, "", "ID DESC", "")
        }

        fun getById(projectId: Long): Project? {
            return findById(Project::class.java, projectId)
        }

    }

}