
if [[ "$(aws --version)" != "aws-cli"* ]]; then
  echo "Installing Aws Cli..."
  curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
  unzip awscliv2.zip
  sudo ./aws/install
  rm awscliv2.zip
  echo "Aws Cli Installation Finished"
fi


if [[ -z "$(aws s3 ls)" ]]; then
  echo "Configuring aws"
  aws_secret_arr=()
  while IFS= read -r line; do
    aws_secret_arr+=("$line")
  done < "./aws_secret.txt"
  aws configure set aws_access_key_id ${aws_secret_arr[0]}
  aws configure set aws_secret_access_key ${aws_secret_arr[1]}
  aws configure set default.region eu-central-1
fi

./gradlew cineast-api:fatJar

echo "Uploading jar to s3 bucket"

aws s3 cp ./cineast-api/build/libs/cineast-api-3.0.1-full.jar s3://vitrivr-config-files/ --acl public-read