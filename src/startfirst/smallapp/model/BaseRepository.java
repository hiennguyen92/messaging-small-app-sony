package startfirst.smallapp.model;

import java.util.ArrayList;

import android.content.ContentResolver;

public abstract class BaseRepository<T> {
	protected ContentResolver mContentResolver;
	
	public BaseRepository(ContentResolver contentResolver) {
		this.mContentResolver = contentResolver;
	}
	
	
	public abstract ArrayList<T> getAll();
	public abstract T get(String Id);

}
