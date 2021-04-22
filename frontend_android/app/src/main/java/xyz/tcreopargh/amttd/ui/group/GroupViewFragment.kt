package xyz.tcreopargh.amttd.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.tcreopargh.amttd.R
import xyz.tcreopargh.amttd.data.group.WorkGroup
import java.util.*

/**
 * @author TCreopargh
 */
class GroupViewFragment : Fragment() {

    private lateinit var groupViewModel: GroupViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        groupViewModel =
            ViewModelProvider(this).get(GroupViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_group_view, container, false)
        groupViewModel.groups.value?.add(WorkGroup(UUID.randomUUID(), "WorkGroup"))
        val groupRecyclerView = view.findViewById<RecyclerView>(R.id.groupRecyclerView)
        val adapter = GroupViewAdapter(groupViewModel.groups)
        groupRecyclerView.adapter = adapter
        groupRecyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}