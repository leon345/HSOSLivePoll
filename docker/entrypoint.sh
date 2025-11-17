#!/bin/bash

echo "Starting LivePoll Application..."

mkdir -p /app/db

if [ ! -z "$DATABASE_NAME" ]; then
    sed -i "s|database.name=.*|database.name=$DATABASE_NAME|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/database.properties
fi

if [ ! -z "$DATABASE_USER" ]; then
    sed -i "s|database.user=.*|database.user=$DATABASE_USER|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/database.properties
fi

if [ ! -z "$DATABASE_PASSWORD" ]; then
    sed -i "s|database.password=.*|database.password=$DATABASE_PASSWORD|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/database.properties
fi

if [ ! -z "$HIBERNATE_DIALECT" ]; then
    sed -i "s|hibernate.dialect=.*|hibernate.dialect=$HIBERNATE_DIALECT|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/database.properties
fi

if [ ! -z "$HIBERNATE_HBM2DDL_AUTO" ]; then
    sed -i "s|hibernate.hbm2ddl.auto=.*|hibernate.hbm2ddl.auto=$HIBERNATE_HBM2DDL_AUTO|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/database.properties
fi

if [ ! -z "$HIBERNATE_SHOW_SQL" ]; then
    sed -i "s|hibernate.show_sql=.*|hibernate.show_sql=$HIBERNATE_SHOW_SQL|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/database.properties
fi

if [ ! -z "$HIBERNATE_FORMAT_SQL" ]; then
    sed -i "s|hibernate.format_sql=.*|hibernate.format_sql=$HIBERNATE_FORMAT_SQL|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/database.properties
fi

if [ ! -z "$AAD_CLIENT_ID" ]; then
    sed -i "s|aad.clientId=.*|aad.clientId=$AAD_CLIENT_ID|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/authentication.properties
fi

if [ ! -z "$AAD_SECRET" ]; then
    sed -i "s|aad.secret=.*|aad.secret=$AAD_SECRET|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/authentication.properties
fi

if [ ! -z "$AAD_AUTHORITY" ]; then
    sed -i "s|aad.authority=.*|aad.authority=$AAD_AUTHORITY|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/authentication.properties
fi

if [ ! -z "$APP_HOME_PAGE" ]; then
    sed -i "s|app.homePage=.*|app.homePage=$APP_HOME_PAGE|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/authentication.properties
fi

if [ ! -z "$APP_DASHBOARD" ]; then
    sed -i "s|app.dashboard=.*|app.dashboard=$APP_DASHBOARD|g" /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/authentication.properties
fi

if [ ! -z "$JAVA_OPTS" ]; then
    export CATALINA_OPTS="$JAVA_OPTS"
fi

echo "Configuration complete, starting Tomcat..."
exec catalina.sh run
