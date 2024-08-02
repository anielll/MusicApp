package com.couturier.musicapp
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.couturier.musicapp.PlaylistData.Companion.readPlaylistDataFromFile
import com.couturier.musicapp.PlaylistData.Companion.writePlaylistDataToFile
class AddSongToPlaylistFragment : DialogFragment() {
    private var libraryIndex: Int? = null
    companion object {

        fun newInstance(index: Int): AddSongToPlaylistFragment {
            val fragment = AddSongToPlaylistFragment()
            val args = Bundle()
            args.putInt("library_index", index)
            fragment.arguments = args
            return fragment
        }
    }
    inner class PlaylistListAdapter (
        private val playlistObjects: MutableList<PlaylistData>,
        private val clickListener: (Int) -> Unit
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
            return PlaylistViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val playlistViewHolder = holder as PlaylistViewHolder
            playlistViewHolder.bind(playlistObjects[position],clickListener)
        }

        override fun getItemCount(): Int {
            return playlistObjects.size
        }

        inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val playlistNameNameTextView: TextView = itemView.findViewById(R.id.playlist_name)
            private val playlistOptionsButton: ImageButton = itemView.findViewById(R.id.playlist_options_button)
            private val playlistIcon: ImageView = itemView.findViewById(R.id.playlist_icon)
            fun bind(playlistObject: PlaylistData,clickListener: (Int) -> Unit) {
                playlistNameNameTextView.text = playlistObject.playlistName
                (playlistOptionsButton.parent as? ViewGroup)?.removeView(playlistOptionsButton)
                if(playlistObject.icon!=null){
                    playlistIcon.setImageBitmap(playlistObject.icon)
                }
                itemView.setOnClickListener {clickListener(playlistObjects[bindingAdapterPosition].fileIndex)}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryIndex = arguments?.getInt("library_index")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_song_to_playlist, container, false)
        val  playlistFileIndexes = MasterList.get()

        val allPlaylistObjects = playlistFileIndexes.mapNotNull {
                readPlaylistDataFromFile(requireContext(),it)
        }
        val playlistObjects = allPlaylistObjects.mapNotNull {
            if(it.songList.contains(libraryIndex)){
                null
            }else{
                it
            }
        }.toMutableList()
        val recyclerView = view.findViewById<RecyclerView>(R.id.playlist_list_recycler_view)
        val songViewAdapter = PlaylistListAdapter(
                playlistObjects,
                clickListener = { fileIndex ->
                    addToPlaylist(fileIndex)
                    dismiss()
                }
            )
            recyclerView.adapter = songViewAdapter
        return view
    }
    private fun addToPlaylist(fileIndex: Int){
        val otherPlaylist = readPlaylistDataFromFile(requireContext(),fileIndex)
        otherPlaylist!!.songList.add(libraryIndex!!)
        writePlaylistDataToFile(requireContext(),otherPlaylist)
        Toast.makeText(requireContext(), "Successfully Saved Song To: ${otherPlaylist.playlistName}", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        val window = requireDialog().window
        val params = window!!.attributes
        val displayMetrics = resources.displayMetrics
        params.width = ViewGroup.LayoutParams.MATCH_PARENT // Full screen width
        params.height = (displayMetrics.heightPixels * 0.5).toInt() // Half the screen height
        window.attributes = params
    }


}