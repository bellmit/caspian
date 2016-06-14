#!/bin/sh
#####################################################################
# Removes all docker images except for the image used to build
#####################################################################

# Given an image tag, find the image ID
get_image_id() {
    local NAME=${1%:*}
    local VERSION=${1##*:}
    local ID=$(docker images "${NAME}" | grep "\\s$VERSION\\s" | awk '{ print $3 }')
    echo "${ID}"
}

# Find the image used to build
DIR=$( cd "$( dirname "$0" )" && pwd )
BUILD_IMAGE=$(grep "DOCKER_IMAGE=" "${DIR}/build.sh" | sed -e 's/DOCKER_IMAGE="${DOCKER_IMAGE:-\(.*\)}"/\1/')
BASE_IMAGES=$(find ${DIR}/.. -name Dockerfile -exec grep "^FROM " \{\} \; | sed -e 's/^FROM //g' | sort | uniq)

echo "Build Image: ${BUILD_IMAGE}"
echo "Base Images: ${BASE_IMAGES}"

# All docker images
IMAGE_IDS=$(docker images -q | sort | uniq)

# Filters out build images
for IMAGE in ${BUILD_IMAGE} ${BASE_IMAGES}; do
    ID=$(get_image_id "${IMAGE}")
    if [ -n "${ID}" ]; then
        IMAGE_IDS=$(echo "${IMAGE_IDS}" | grep -v "${ID}")
    fi
done

# Remove all images
if [ -n "${IMAGE_IDS}" ]; then
    echo "Images to delete:" ${IMAGE_IDS}
    if [ "$1" != "--no-prompt" ]; then
        IMAGE_COUNT=$(echo "$IMAGE_IDS" | wc -l)
        read -p "Proceed with deleting $IMAGE_COUNT image(s)? [yes|no] " PROMPT
        if [ "${PROMPT}" != "yes" ]; then
            echo "  Aborting"
            exit 1
        fi
    fi
    docker rmi -f ${IMAGE_IDS}
else
    echo "No images to delete"
fi


