package plm.core.model.tracking;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import plm.core.model.Game;
import plm.core.model.lesson.Exercise;

public class IdenticaSpy implements ProgressSpyListener {
    private String username;

    public IdenticaSpy() {
        username = System.getenv("USER");
        if (username == null)
            username = System.getenv("USERNAME");
        if (username == null)
            username = "John Doe";

    }

    @Override
    public void executed(Exercise exo) {
        if (Game.getInstance().studentWork.getPassed(exo, exo.lastResult.language)) {

            DefaultHttpClient httpclient = new DefaultHttpClient();
            try {
                HttpPost post = new HttpPost(new URI("http://identi.ca/api/statuses/update.json"));

                httpclient.getCredentialsProvider().setCredentials(
                        new AuthScope("identi.ca", 80),
                        new UsernamePasswordCredentials(Game.getProperty("plm.identica.username"),
                                Game.getProperty("plm.identica.password")));

                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("status", username + " solved " + exo.getName() + " in " + exo.lastResult.language + "!"));
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                post.setEntity(entity);

                httpclient.execute(post);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void switched(Exercise exo) {    /* i don't care, i'm a viking */ }

    @Override
    public void heartbeat(){    /* i don't care, i'm a viking */ }

    @Override
    public String join() { return ""; }

    @Override
    public void leave() {
    }
}
