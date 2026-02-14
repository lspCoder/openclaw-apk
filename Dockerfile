FROM ubuntu:22.04

# Maintainer
LABEL maintainer="OpenClaw Team"

# Install JDK and required tools in one layer
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        openjdk-17-jdk-headless \
        wget \
        unzip \
        zip \
        curl \
        git \
        lib32stdc++6 \
        lib32z1 \
        libc6:i386 \
        libncurses5:i386 \
        libstdc++6:i386 \
        libgcc-11-dev:i386 \
        python3 \
        && rm -rf /var/lib/apt/lists/* \
        && apt-get clean

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Environment variables
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH

# Download and setup Android command-line tools
RUN mkdir -p $ANDROID_SDK_ROOT/cmdline-tools && \
    cd $ANDROID_SDK_ROOT/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip && \
    unzip -q cmdline-tools.zip && \
    mv cmdline-tools latest && \
    rm cmdline-tools.zip

# Accept licenses and install SDK components
RUN yes | sdkmanager --licenses 2>/dev/null || true
RUN sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"

# Create project directory
WORKDIR /workspace

# Default command
CMD ["/bin/bash"]
