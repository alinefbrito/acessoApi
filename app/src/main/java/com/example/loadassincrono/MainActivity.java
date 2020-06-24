package com.example.loadassincrono;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private EditText nmLivro;
    private TextView nmTitulo;
    private TextView nmAutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nmLivro = findViewById(R.id.livroInput);
        nmTitulo = findViewById(R.id.tituloText);
        nmAutor = findViewById(R.id.autorText);
        if (getSupportLoaderManager().getLoader(0) != null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    public void buscaLivros(View view) {
        // Recupera a string de busca.
        String queryString = nmLivro.getText().toString();
        // esconde o teclado qdo o botão é clicado
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // Verifica o status da conexão de rede
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        /* Se a rede estiver disponivel e o campo de busca não estiver vazio
         iniciar o Loader CarregaLivros */
        if (networkInfo != null && networkInfo.isConnected()
                && queryString.length() != 0) {
            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", queryString);
            getSupportLoaderManager().restartLoader(0, queryBundle, this);
            nmAutor.setText(R.string.str_empty);
            nmTitulo.setText(R.string.loading);
        }
        // atualiza a textview para informar que não há conexão ou termo de busca
        else {
            if (queryString.length() == 0) {
                nmAutor.setText(R.string.str_empty);
                nmTitulo.setText(R.string.no_search_term);
            } else {
                nmAutor.setText(" ");
                nmTitulo.setText(R.string.no_network);
            }
        }
    }
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        String queryString = "";
        if (args != null) {
            queryString = args.getString("queryString");
        }
        return new CarregaLivros(this, queryString);
    }
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        try {
            // Converte a resposta em Json
            JSONObject jsonObject = new JSONObject(data);
            // Obtem o JSONArray dos itens de livros
            JSONArray itemsArray = jsonObject.getJSONArray("items");
            // inicializa o contador
            int i = 0;
            String titulo = null;
            String autor = null;
            // Procura pro resultados nos itens do array
            while (i < itemsArray.length() &&
                    (autor == null && titulo == null)) {
                // Obtem a informação
                JSONObject book = itemsArray.getJSONObject(i);
                JSONObject volumeInfo = book.getJSONObject("volumeInfo");
                //  Obter autor e titulo para o item,
                // erro se o campo estiver vazio
                try {
                    titulo = volumeInfo.getString("title");
                    autor = volumeInfo.getString("authors");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // move para a proxima linha
                i++;
            }
            //mostra o resultado qdo possivel.
            if (titulo != null && autor != null) {
                nmTitulo.setText(titulo);
                nmAutor.setText(autor);
                //nmLivro.setText(R.string.str_empty);
            } else {
                // If none are found, update the UI to show failed results.
                nmTitulo.setText(R.string.no_results);
                nmAutor.setText(R.string.str_empty);
            }
        } catch (Exception e) {
            // Se não receber um JSOn valido, informa ao usuário
            nmTitulo.setText(R.string.no_results);
            nmAutor.setText(R.string.str_empty);
            e.printStackTrace();
        }
    }
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
        // obrigatório implemntar, nenhuma ação executada
    }
}