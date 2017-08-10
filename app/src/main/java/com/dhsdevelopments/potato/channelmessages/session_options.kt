package com.dhsdevelopments.potato.channelmessages

import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.clientapi.notifications.Option
import com.dhsdevelopments.potato.clientapi.notifications.OptionNotification
import com.dhsdevelopments.potato.imagecache.ImageCache
import com.dhsdevelopments.potato.service.RemoteRequestService
import com.koushikdutta.ion.Ion
import com.koushikdutta.ion.builder.AnimateGifMode

class OptionsDialogFragment : DialogFragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var optionsNotification: OptionNotification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog)

        optionsNotification = arguments.getSerializable("options") as OptionNotification
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_options_dialog, container, false)

        val title = view.findViewById<TextView>(R.id.title_text_view)
        title.text = optionsNotification.title

        recyclerView = view.findViewById<RecyclerView>(R.id.options_recycler_view)
        //recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        //recyclerView.addItemDecoration(MarginDecoration)
        recyclerView.layoutManager = GridLayoutManager(activity, 3)
        recyclerView.adapter = OptionsAdapter(this, optionsNotification)

        return view;
    }

    internal fun optionSelected(code: String) {
        RemoteRequestService.sendCommand(activity, optionsNotification.channel, optionsNotification.optionCode, code, true)
        dialog.dismiss()
    }

    companion object {
        fun makeDialog(notification: OptionNotification): OptionsDialogFragment {
            val fragment = OptionsDialogFragment()
            val args = Bundle()
            args.putSerializable("options", notification)
            fragment.arguments = args
            return fragment
        }
    }
}

class OptionsAdapter(private val parent: OptionsDialogFragment, notification: OptionNotification) : RecyclerView.Adapter<OptionsAdapter.ViewHolder>() {
    private val options: List<OptionWrapper> = notification.options.map { OptionWrapper(it) }
    private lateinit var imageCache: ImageCache

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        imageCache = ImageCache(parent.activity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.option_dialog_view, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.fillInView(options[pos])
    }

    override fun getItemCount(): Int {
        return options.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleView = itemView.findViewById<TextView>(R.id.option_title)
        val imageView = itemView.findViewById<ImageView>(R.id.option_image)
        val selectButton = itemView.findViewById<Button>(R.id.option_select_button)
        var updateIndex = 0
        var currentWrapper: OptionWrapper? = null

        init {
            selectButton.setOnClickListener { selectClicked() }
        }

        private fun selectClicked() {
            val response = currentWrapper!!.option.response
            parent.optionSelected(response)
        }

        fun fillInView(wrapper: OptionWrapper) {
            this.currentWrapper = wrapper
            val option = wrapper.option

            titleView.text = option.title

            if (option.imageUrl != null) {
                val oldUpdateIndex = ++updateIndex
                val res = parent.activity.resources
                val imageWidth = res.getDimensionPixelSize(R.dimen.option_image_width)
                val imageHeight = res.getDimensionPixelSize(R.dimen.option_image_height)

                //                imageCache.loadImage(option.imageUrl!!, imageWidth, imageHeight, StorageType.DONT_STORE,
                //                        object : LoadImageCallback {
                //                            override fun bitmapLoaded(bitmap: Bitmap) {
                //                                if (updateIndex == oldUpdateIndex) {
                //                                    imageView.setImageBitmap(bitmap)
                //                                }
                //                            }
                //
                //                            override fun bitmapNotFound() {
                //                                if (updateIndex == oldUpdateIndex) {
                //                                    imageView.setImageDrawable(ColorDrawable(Color.GREEN))
                //                                }
                //                            }
                //                        })

                Ion.with(parent.activity)
                        .load(option.imageUrl!!)
                        .withBitmap()
                        .placeholder(R.drawable.image_load_animation)
                        .animateGif(AnimateGifMode.ANIMATE)
                        .intoImageView(imageView)

                //                Picasso.with(parent.context)
                //                        .load(option.imageUrl!!)
                //                        .into(imageView)

                imageView.visibility = View.VISIBLE
            }
            else {
                imageView.visibility = View.GONE
            }

            selectButton.text = option.buttonText ?: "Select"
        }
    }
}

class OptionWrapper(var option: Option)
