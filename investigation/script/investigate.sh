#!/bin/bash

set -e

APP_BIN="./build/install/investigation/bin/investigation"

# for js
ITEMS=(
  "javascript|https://github.com/adobe/brackets.git" # App
  "javascript|https://github.com/moment/moment.git" # Non-web
  "javascript|https://github.com/gulpjs/gulp.git" # Software tool
  "javascript|https://github.com/nodejs/node.git" # System
  "javascript|https://github.com/facebook/react.git" # Web

  "javascript|https://github.com/resume/resume.github.com.git" # Application software (2nd)
  "javascript|https://github.com/lodash/lodash.git"      # Non-web libraries and frameworks (2nd)
  "javascript|https://github.com/yarnpkg/yarn.git"       # Software tools (2nd)
  "javascript|https://github.com/typicode/json-server.git" # System software (2nd)
  "javascript|https://github.com/angular/angular.js.git" # Web libraries and frameworks (2nd)
)

# "lang|repo_url".
# For each language: 5 domains (exclude Documentation), take the 2nd-most-starred repo per domain
# ITEMS=(
  # C
  # "c|https://github.com/tmux/tmux.git"                    # Application software (2nd)
  # "c|https://github.com/torch/torch7.git"                 # Non-web libraries and frameworks (2nd)
  # "c|https://github.com/git/git.git"                     # Software tools (2nd)
  # "c|https://github.com/antirez/redis.git"               # System software (2nd)
  # "c|https://github.com/allinurl/goaccess.git"           # Web libraries and frameworks (2nd)

  # Go
  # "go|https://github.com/spf13/hugo.git"                 # Application software (2nd)
  # "go|https://github.com/go-kit/kit.git"                 # Non-web libraries and frameworks (2nd)
  # "go|https://github.com/gogits/gogs.git"                # Software tools (2nd)
  # "go|https://github.com/golang/go.git"                  # System software (2nd)
  # "go|https://github.com/gin-gonic/gin.git"              # Web libraries and frameworks (2nd)

  # Java
  # "java|https://github.com/hannahmitt/homemirror.git"    # Application software (2nd)
  # "java|https://github.com/google/guava.git"             # Non-web libraries and frameworks (2nd)
  # "java|https://github.com/spring-projects/spring-boot.git" # Software tools (2nd)
  # "java|https://github.com/clojure/clojure.git"          # System software (2nd)
  # "java|https://github.com/spring-projects/spring-framework.git" # Web libraries and frameworks (2nd)

  # JavaScript
  # "javascript|https://github.com/resume/resume.github.com.git" # Application software (2nd)
  # "javascript|https://github.com/lodash/lodash.git"      # Non-web libraries and frameworks (2nd)
  # "javascript|https://github.com/yarnpkg/yarn.git"       # Software tools (2nd)
  # "javascript|https://github.com/typicode/json-server.git" # System software (2nd)
  # "javascript|https://github.com/angular/angular.js.git" # Web libraries and frameworks (2nd)

  # PHP
  # "php|https://github.com/phanan/koel.git"               # Application software (2nd)
  # "php|https://github.com/phpoffice/phpexcel.git"        # Non-web libraries and frameworks (2nd)
  # "php|https://github.com/piwik/piwik.git"               # Software tools (2nd)
  # php has only one popular system software repo
  # "php|https://github.com/bcit-ci/codeigniter.git"      # Web libraries and frameworks (2nd)

  # Python
  # "python|https://github.com/reddit/reddit.git"          # Application software (2nd)
  # "python|https://github.com/scikit-learn/scikit-learn.git" # Non-web libraries and frameworks (2nd)
  # "python|https://github.com/nvbn/thefuck.git"           # Software tools (2nd)
  # "python|https://github.com/samshadwell/trumpscript.git" # System software (2nd)
  # "python|https://github.com/django/django.git"          # Web libraries and frameworks (2nd)

  # Ruby
  # "ruby|https://github.com/discourse/discourse.git"      # Application software (2nd)
  # "ruby|https://github.com/thoughtbot/paperclip.git"     # Non-web libraries and frameworks (2nd)
  # "ruby|https://github.com/cantino/huginn.git"           # Software tools (2nd)
  # "ruby|https://github.com/jruby/jruby.git"              # System software (2nd)
  # "ruby|https://github.com/sinatra/sinatra.git"          # Web libraries and frameworks (2nd)
# )

# rank 1
# ITEMS=(
#   "java|https://github.com/pockethub/pockethub.git" # App
#   "java|https://github.com/nostra13/android-universal-image-loader.git" # Non-web library
#   "java|https://github.com/elastic/elasticsearch.git" # Software tool
#   "java|https://github.com/reactivex/rxjava.git" # System
#   "java|https://github.com/bumptech/glide.git" # Web

#   "c|https://github.com/ffmpeg/ffmpeg.git" # App
#   "c|https://github.com/bilibili/ijkplayer.git" # Non-web library
#   "c|https://github.com/firehol/netdata.git" # Software tool
#   "c|https://github.com/torvalds/linux.git" # System
#   "c|https://github.com/phpredis/phpredis.git" # Web

  # "javascript|https://github.com/adobe/brackets.git" # App
  # "javascript|https://github.com/moment/moment.git" # Non-web
  # "javascript|https://github.com/gulpjs/gulp.git" # Software tool
  # "javascript|https://github.com/nodejs/node.git" # System
  # "javascript|https://github.com/facebook/react.git" # Web
# 
#   "ruby|https://github.com/jekyll/jekyll.git" # App
#   "ruby|https://github.com/plataformatec/devise.git" # Non-web library
#   "ruby|https://github.com/gitlabhq/gitlabhq.git" # Software tool
#   "ruby|https://github.com/ruby/ruby.git" # System
#   "ruby|https://github.com/rails/rails.git" # Web

#   "go|https://github.com/getlantern/lantern.git" # App
#   "go|https://github.com/labstack/echo.git" # Non-web library
#   "go|https://github.com/kubernetes/kubernetes.git" # Software tool
#   "go|https://github.com/docker/docker.git" # System
#   "go|https://github.com/go-martini/martini.git" # Web

#   "php|https://github.com/wordpress/wordpress.git" # App
#   "php|https://github.com/phpmailer/phpmailer.git" # Non-web library
#   "php|https://github.com/composer/composer.git" # Software tool
#   "php|https://github.com/thephpleague/oauth2-server.git" # System
#   "php|https://github.com/symfony/symfony.git" # Web

#   "python|https://github.com/rg3/youtube-dl.git" # App
#   "python|https://github.com/scrapy/scrapy.git" # Non-web library
#   "python|https://github.com/jkbrzt/httpie.git" # Software tool
#   "python|https://github.com/apenwarr/sshuttle.git" # System
#   "python|https://github.com/pallets/flask.git" # Web
# )

# Ensure logs directory exists
mkdir -p logs

# Maximum concurrent processes (adjustable)
MAX_CONCURRENT=15

# Active background PIDs
PIDS=()

for entry in "${ITEMS[@]}"; do
  # parse entry "lang|repo_url"
  lang="${entry%%|*}"
  repo_url="${entry#*|}"

  if [ -z "$lang" ] || [ -z "$repo_url" ] || [ "$lang" = "$repo_url" ]; then
    echo "Skipping malformed entry: $entry"
    continue
  fi

  repo_name=$(basename "$repo_url" .git)
  safe_repo_name=$(echo "$repo_name" | sed 's/[^a-zA-Z0-9_.-]/_/g')
  log_file="logs/out_${lang}_${safe_repo_name}.log"

  # Launch one process per repository, passing --repo to the application
  # "$APP_BIN" --language "$lang" --repo "$repo_url" --resume > "$log_file" 2>&1 &
  "$APP_BIN" --language "$lang" --repo "$repo_url" > "$log_file" 2>&1 &
  pid=$!
  PIDS+=("$pid")
  echo "Started process for $lang repo $repo_url (PID: $pid) -> $log_file"

  # If we've reached the concurrency limit, wait until at least one PID finishes.
  while :; do
    # Prune finished PIDs from PIDS
    active=()
    for p in "${PIDS[@]}"; do
      if kill -0 "$p" >/dev/null 2>&1; then
        active+=("$p")
      fi
    done
    PIDS=("${active[@]}")

    if [ "${#PIDS[@]}" -lt "$MAX_CONCURRENT" ]; then
      break
    fi

    sleep 10
  done
done

# After starting all entries, wait for any remaining background processes
for p in "${PIDS[@]}"; do
  if kill -0 "$p" >/dev/null 2>&1; then
    wait "$p"
  fi
done

echo "All scripts have completed."