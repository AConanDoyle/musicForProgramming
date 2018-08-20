package magnesia.mapapps.de.musicforprogramming.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import magnesia.mapapps.de.musicforprogramming.R;

/**
 * Created by Hagen on 07.02.2018.
 */

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {

        private List<Song> songsList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title, number;
            public MyViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                number = (TextView) view.findViewById(R.id.number);
            }
        }

        public SongsAdapter(List<Song> songsList) {
            this.songsList = songsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.song_list_row, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Song song = songsList.get(position);
            holder.title.setText(song.getTitle());
            holder.number.setText(song.getNumber());
        }

        @Override
        public int getItemCount() {
            return songsList.size();
        }

}
