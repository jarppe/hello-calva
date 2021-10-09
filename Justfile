set dotenv-load := true


help:
  @just --list


# Start shell container:
sh +args='':
  docker run                       \
    --rm -it                       \
    -v $(pwd):/home/dev/app        \
    -v ${HOME}/.m2:/home/dev/.m2   \
    -v hello-calva_data:/data      \
    --network hello-calva_default  \
    --entrypoint bash              \
    hello-calva:dev                \
    {{ args }}


# Attach to server container:
exec +args='bash':
  docker exec                      \
    -it                            \
    -u dev:dev                     \
    -w /home/dev/app               \
    hello-calva_server_1           \
    {{ args }}
