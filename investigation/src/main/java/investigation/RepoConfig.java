package investigation;

import java.util.Map;

// TODO: delete this class and method that uses this.

public final class RepoConfig {

    public static final String[] JAVA = {
        // App
        // "https://github.com/HannahMitt/HomeMirror.git",
        // "https://github.com/nostra13/Android-Universal-Image-Loader.git",
        // "https://github.com/elastic/elasticsearch.git",
        // "https://github.com/ReactiveX/RxJava.git",
        // "https://github.com/spring-projects/spring-framework.git",

        // Borges 2018
        "https://github.com/pockethub/pockethub.git", // App
        "https://github.com/nostra13/android-universal-image-loader.git", // Non-web library
        "https://github.com/elastic/elasticsearch.git", // Software tool
        "https://github.com/reactivex/rxjava.git", // System
        "https://github.com/bumptech/glide.git", // Web
    };

    public static final String[] C = {
        // "https://github.com/b4winckler/macvim.git",
        // "https://github.com/liuliu/ccv.git",
        // "https://github.com/git/git.git",
        // "https://github.com/torvalds/linux.git",
        // "https://github.com/vmg/redcarpet.git",

        // Borges 2018
        "https://github.com/ffmpeg/ffmpeg.git", // App 122483 commits
        "https://github.com/bilibili/ijkplayer.git", // Non-web library
        "https://github.com/firehol/netdata.git", // Software tool
        "https://github.com/torvalds/linux.git", // System 1,413,527 commits
        "https://github.com/phpredis/phpredis.git", // Web
    };

    public static final String[] JAVASCRIPT = {
        // "https://github.com/adobe/brackets.git",
        // "https://github.com/moment/moment.git",
        // "https://github.com/gulpjs/gulp.git",
        // "https://github.com/nodejs/node.git",
        // "https://github.com/angular/angular.js.git",

        // Borges 2018
        "https://github.com/adobe/brackets.git", // App 17847 commits
        "https://github.com/moment/moment.git", // Non-web
        "https://github.com/gulpjs/gulp.git", // Software tool
        "https://github.com/nodejs/node.git", // System 45842 commits
        "https://github.com/facebook/react.git", // Web 21278 commits
    };

    public static final String[] RUBY = {
        // "https://github.com/rails/rails.git",
        // "https://github.com/maybe-finance/maybe.git",

        // Borges 2016
        // "https://github.com/jekyll/jekyll.git", // App
        // "https://github.com/plataformatec/devise.git", // Non-web library
        // "https://github.com/gitlabhq/gitlabhq", // Software tool
        // "https://github.com/ruby/ruby.git", // System
        // "https://github.com/rails/rails.git", // Web

        // Borges 2018
        "https://github.com/jekyll/jekyll.git", // App
        "https://github.com/plataformatec/devise.git", // Non-web library
        "https://github.com/gitlabhq/gitlabhq.git", // Software tool
        "https://github.com/ruby/ruby.git", // System 96694 commits
        "https://github.com/rails/rails.git", // Web 96897 commits
    };


    public static final String[] GO = {
        // "https://github.com/ollama/ollama.git",
        // "https://github.com/golang/go.git",

        // Borges 2016
        // "https://github.com/getlantern/lantern.git", // App
        // "https://github.com/golang/groupcache.git", // Non-web
        // "https://github.com/kubernetes/kubernetes.git", // Software tool
        // "https://github.com/moby/moby.git", // System
        // "https://github.com/go-martini/martini.git", // Web

        // Borges 2018
        "https://github.com/getlantern/lantern.git", // App
        "https://github.com/labstack/echo.git", // Non-web library
        "https://github.com/kubernetes/kubernetes.git", // Software tool 134911 commits
        "https://github.com/docker/docker.git", // System
        "https://github.com/go-martini/martini.git", // Web
    };

    public static final String[] PHP = {
        // "https://github.com/laravel/framework.git",
        // "https://github.com/nextcloud/server.git",

        // Borges 2016
        // "https://github.com/WordPress/WordPress.git", // App
        // "https://github.com/PHPMailer/PHPMailer.git", // Non-web library
        // "https://github.com/composer/composer.git", // Software tool
        //  System category is not available for PHP
        // "https://github.com/bcit-ci/CodeIgniter.git", // Web

        // Borges 2018
        "https://github.com/wordpress/wordpress.git", // App
        "https://github.com/phpmailer/phpmailer.git", // Non-web library
        "https://github.com/composer/composer.git", // Software tool
        "https://github.com/thephpleague/oauth2-server.git", // System
        "https://github.com/symfony/symfony.git", // Web
    };

    public static final String[] PYTHON = {
        // "https://github.com/Significant-Gravitas/AutoGPT.git",
        // "https://github.com/huggingface/transformers.git",
        // "https://github.com/ytdl-org/youtube-dl.git"

        // Borges 2016
        // "https://github.com/ytdl-org/youtube-dl.git", // App
        // "https://github.com/scrapy/scrapy.git", // Non-web library
        // "https://github.com/jkbrzt/httpie.git", // Software tool
        // "https://github.com/apenwarr/sshuttle.git", // System
        // "https://github.com/django/django.git", // Web"

        // Borges 2018
        "https://github.com/rg3/youtube-dl.git", // App
        "https://github.com/scrapy/scrapy.git", // Non-web library
        "https://github.com/jkbrzt/httpie.git", // Software tool
        "https://github.com/apenwarr/sshuttle.git", // System
        "https://github.com/pallets/flask.git", // Web
    };

    public static final Map<String, String[]> REPOS_BY_LANGUAGE = Map.of(
        "Java", JAVA,
        "C", C,
        "JavaScript", JAVASCRIPT,
        "Ruby", RUBY,
        "Go", GO,
        "PHP", PHP,
        "Python", PYTHON
    );

    private RepoConfig() {}
}
