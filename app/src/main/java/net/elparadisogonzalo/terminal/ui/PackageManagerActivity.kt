package net.elparadisogonzalo.terminal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.elparadisogonzalo.terminal.R
import net.elparadisogonzalo.terminal.core.PackageRepository
import net.elparadisogonzalo.terminal.databinding.ActivityPackagesBinding

class PackageManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPackagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = Adapter(PackageRepository.featured)
    }

    private class Adapter(val items: List<Pair<String, String>>) :
        RecyclerView.Adapter<Adapter.VH>() {

        class VH(v: android.view.View) : RecyclerView.ViewHolder(v) {
            val name: TextView = v.findViewById(R.id.pkg_name)
            val desc: TextView = v.findViewById(R.id.pkg_desc)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_package, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, position: Int) {
            val (n, d) = items[position]
            h.name.text = n
            h.desc.text = d
            h.itemView.setOnClickListener {
                android.widget.Toast.makeText(
                    h.itemView.context, "pkg install $n", android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun getItemCount() = items.size
    }
}
