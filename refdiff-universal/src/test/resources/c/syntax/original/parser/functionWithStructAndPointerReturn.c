struct Cookie *
Curl_cookie_add(struct Curl_easy *data,
                /* The 'data' pointer here may be NULL at times, and thus
                   must only be used very carefully for things that can deal
                   with data being NULL. Such as infof() and similar */

                struct CookieInfo *c,
                bool httpheader, /* TRUE if HTTP header-style line */
                bool noexpire, /* if TRUE, skip remove_expired() */
                char *lineptr,   /* first character of the line */
                const char *domain, /* default domain */
                const char *path)   /* full path used when this cookie is set,
                                       used to get default path for the cookie
                                       unless set */
{
}
