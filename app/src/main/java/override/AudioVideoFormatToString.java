package override;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.github.kiulian.downloader.model.formats.AudioVideoFormat;

public class AudioVideoFormatToString extends AudioVideoFormat {

    public AudioVideoFormatToString(JSONObject json, boolean isAdaptive) {
        super(json, isAdaptive);
    }

    @NonNull
    @Override
    public String toString() {
        return qualityLabel();
    }
}
