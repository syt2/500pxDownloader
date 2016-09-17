package party.danyang.a500pxdownloader;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by dream on 16-8-19.
 */
public class Api {
    private static final String BASE_URL = "https://500px.com/";

    public interface HtmlApi {
        @GET("photo/{code}")
        Call<String> load(@Path("code") String code);
    }

    public static final Call<String> loadHtml(String code) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        return retrofit.create(HtmlApi.class).load(code);
    }


}
