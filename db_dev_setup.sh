echo "Welcome to ProductiviTeam development setup!"
echo "---"
echo "Please make sure:"
echo "1. You have all dependencies installed"
echo "2. Containerized PostgreSQL database is running (see documentation for instructions)"
echo "3. YOU HAVE FILLED IN THIS FILE WITH THE CORRECT VALUES"
echo "---"
echo ""

# ----------------- ENVIRONMENT VARIABLES -----------------
export DB_PSW=
export DB_UNM=
export DB_URL=
export DB_DDL_AUTO=
export PUSHER_APP_ID=
export PUSHER_KEY=
export PUSHER_SECRET=
export MAILJET_KEY=
export MAILJET_SECRET=
export MAILJET_SENDER_EMAIL=
export AGORA_APP_ID=
export AGORA_APP_KEY=
export AGORA_APP_CERTIFICATE=
# ---------------------------------------------------------

# ask the developer which profile to use and read the input
echo "Which profile do you want to use?"
echo "1. dev: for testing"
echo "2. devRun: for running the application in development mode"
echo -n " (dev, devRun): "
read profile
export SPRING_PROFILES_ACTIVE=$profile
clear

# print env variables:
echo "Environment variables set for $profile profile are as follows:"
echo "SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE"
echo "DB_PSW=$DB_PSW"
echo "DB_UNM=$DB_UNM"
echo "DB_URL=$DB_URL"
echo "DB_DDL_AUTO=$DB_DDL_AUTO"
echo "PUSHER_APP_ID=$PUSHER_APP_ID"
echo "PUSHER_KEY=$PUSHER_KEY"
echo "PUSHER_SECRET=$PUSHER_SECRET"
echo "MAILJET_KEY=$MAILJET_KEY"
echo "MAILJET_SECRET=$MAILJET_SECRET"
echo "MAILJET_SENDER_EMAIL=$MAILJET_SENDER_EMAIL"
echo "AGORA_APP_ID=$AGORA_APP_ID"
echo "AGORA_APP_KEY=$AGORA_APP_KEY"
echo "AGORA_APP_CERTIFICATE=$AGORA_APP_CERTIFICATE"

echo "---"
echo "You can now run the application using the following command:"
echo "- './gradlew test' to run the tests (make sure to have DEV profile loaded)"
echo "- './gradlew build' to build the application (make sure to have DEV profile loaded)"
echo "- './gradlew bootRun' to run the application (make sure to have DEVRUN profile loaded)"
echo "  * for this, the pusher will be tested actively due to tests on deployment (make sure to have the correct pusher credentials)"
