package xyz.tcreopargh.amttd.ui.group

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.group_view_fragment.*
import okhttp3.Request
import xyz.tcreopargh.amttd.AMTTD
import xyz.tcreopargh.amttd.MainActivity
import xyz.tcreopargh.amttd.R
import xyz.tcreopargh.amttd.common.bean.request.WorkGroupActionRequest
import xyz.tcreopargh.amttd.common.bean.request.WorkGroupViewRequest
import xyz.tcreopargh.amttd.common.bean.response.WorkGroupActionResponse
import xyz.tcreopargh.amttd.common.bean.response.WorkGroupViewResponse
import xyz.tcreopargh.amttd.common.data.CrudType
import xyz.tcreopargh.amttd.common.data.IWorkGroup
import xyz.tcreopargh.amttd.common.data.WorkGroupImpl
import xyz.tcreopargh.amttd.common.exception.AmttdException
import xyz.tcreopargh.amttd.ui.FragmentOnMainActivityBase
import xyz.tcreopargh.amttd.util.*
import java.util.*

/**
 * @author TCreopargh
 */
class GroupViewFragment : FragmentOnMainActivityBase() {

    companion object {
        fun newInstance() = GroupViewFragment()
    }

    lateinit var viewModel: GroupViewModel

    private lateinit var groupSwipeContainer: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.group_view_fragment, container, false)
        val groupRecyclerView = view.findViewById<RecyclerView>(R.id.groupRecyclerView)
        groupSwipeContainer = view.findViewById(R.id.groupSwipeContainer)
        val adapter = GroupViewAdapter(viewModel.groups.value ?: mutableListOf(), this)
        groupRecyclerView.adapter = adapter
        groupRecyclerView.layoutManager = LinearLayoutManager(context)
        groupSwipeContainer.setOnRefreshListener { initializeItems() }
        viewModel.groups.observe(viewLifecycleOwner) {
            adapter.workGroups = it ?: mutableListOf()
            adapter.notifyDataSetChanged()
            groupSwipeContainer.isRefreshing = false
        }
        viewModel.dirty.observe(viewLifecycleOwner) {
            if (it) {
                initializeItems()
                viewModel.dirty.value = false
            }
        }

        viewModel.exception.observe(viewLifecycleOwner) {
            it?.run {
                Toast.makeText(
                    context,
                    getString(R.string.error_occured) + it.getLocalizedString(context),
                    Toast.LENGTH_SHORT
                ).show()
                groupSwipeContainer.isRefreshing = false
                viewModel.exception.value = null
            }
        }

        viewModel.groupToEdit.observe(viewLifecycleOwner) {
            it?.run {
                AlertDialog.Builder(context).apply {
                    @SuppressLint("InflateParams")
                    val viewRoot = layoutInflater.inflate(R.layout.group_edit_layout, null)
                    val titleText = viewRoot.findViewById<EditText>(R.id.groupEditTitleText)
                    titleText.setText(it.name)
                    setView(viewRoot)
                    setPositiveButton(R.string.confirm) { dialog, _ ->
                        object :
                            CrudTask<WorkGroupImpl, WorkGroupActionRequest, WorkGroupActionResponse>(
                                request = WorkGroupActionRequest(
                                    operation = CrudType.UPDATE,
                                    entity = WorkGroupImpl(it).apply {
                                        name = titleText.text.toString()
                                    },
                                    userId = (activity as? MainActivity)?.loggedInUser?.uuid
                                ),
                                path = "/workgroup",
                                responseType = object : TypeToken<WorkGroupActionResponse>() {}.type
                            ) {
                            override fun onSuccess(entity: WorkGroupImpl?) {
                                Thread.sleep(200)
                                viewModel.dirty.postValue(true)
                            }

                            override fun onFailure(e: Exception) {
                                viewModel.exception.postValue(AmttdException.getFromException(e))
                            }
                        }.execute()
                        dialog.cancel()
                    }
                    setNeutralButton(R.string.remove) { dialog, _ ->
                        object :
                            CrudTask<WorkGroupImpl, WorkGroupActionRequest, WorkGroupActionResponse>(
                                request = WorkGroupActionRequest(
                                    operation = CrudType.DELETE,
                                    entity = WorkGroupImpl(it),
                                    userId = (activity as? MainActivity)?.loggedInUser?.uuid
                                ),
                                path = "/workgroup",
                                responseType = object : TypeToken<WorkGroupActionResponse>() {}.type
                            ) {
                            override fun onSuccess(entity: WorkGroupImpl?) {
                                Thread.sleep(200)
                                viewModel.dirty.postValue(true)
                            }

                            override fun onFailure(e: Exception) {
                                viewModel.exception.postValue(AmttdException.getFromException(e))
                            }
                        }.execute()
                        dialog.cancel()
                    }
                    setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
                }.create().show()
                viewModel.groupToEdit.value = null
            }
        }

        val mainActivity = activity as? MainActivity
        mainActivity?.viewModel?.loginRepository?.observe(mainActivity) {
            initializeItems()
        }
        initializeItems()
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(this).get(GroupViewModel::class.java)
        setHasOptionsMenu(true)
    }

    fun addWorkGroup() {
        AlertDialog.Builder(context).apply {
            @SuppressLint("InflateParams")
            val viewRoot = layoutInflater.inflate(R.layout.group_edit_layout, null)
            val titleText = viewRoot.findViewById<EditText>(R.id.groupEditTitleText)
            titleText.setText("")
            setView(viewRoot)
            setPositiveButton(R.string.confirm) { dialog, _ ->
                object : CrudTask<WorkGroupImpl, WorkGroupActionRequest, WorkGroupActionResponse>(
                    request = WorkGroupActionRequest(
                        operation = CrudType.CREATE,
                        entity = WorkGroupImpl().apply {
                            name = titleText.text.toString()
                        },
                        userId = (activity as? MainActivity)?.loggedInUser?.uuid
                    ),
                    path = "/workgroup",
                    responseType = object : TypeToken<WorkGroupActionResponse>() {}.type
                ) {
                    override fun onSuccess(entity: WorkGroupImpl?) {
                        Thread.sleep(500)
                        viewModel.dirty.postValue(true)
                    }

                    override fun onFailure(e: Exception) {
                        viewModel.exception.postValue(AmttdException.getFromException(e))
                    }
                }.execute()
                dialog.cancel()
            }
            setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        }.create().show()
    }

    private fun initializeItems() {
        groupSwipeContainer.isRefreshing = true
        Thread {
            val workGroups: List<IWorkGroup> = try {
                val uuid = (activity as? MainActivity)?.loggedInUser?.uuid ?: return@Thread
                val request = Request.Builder()
                    .post(
                        WorkGroupViewRequest(uuid).toJsonRequest()
                    ).url(rootUrl.withPath("/workgroups"))
                    .build()
                val response = AMTTD.okHttpClient.newCall(request).execute()
                val body = response.body?.string()
                // Don't simplify this
                val result: WorkGroupViewResponse =
                    gson.fromJson(body, object : TypeToken<WorkGroupViewResponse>() {}.type)
                if (result.success != true) {
                    throw AmttdException.getFromErrorCode(result.error)
                }
                result.workGroups ?: throw RuntimeException("Invalid data")
            } catch (e: Exception) {
                Log.e(AMTTD.logTag, e.stackTraceToString())
                viewModel.exception.postValue(AmttdException.getFromException(e))
                listOf()
            }
            viewModel.postGroup(workGroups.toMutableList())
        }.start()
    }

}