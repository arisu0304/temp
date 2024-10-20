import React, { useEffect, useRef } from "react";
import Hls from "hls.js";

const VideoSection = ({ streamingUrl }) => {
  
  const videoRef = useRef(null);

  useEffect(() => {
    if (streamingUrl && streamingUrl.length > 0) {
      const video = videoRef.current;
      if (Hls.isSupported()) {
        const hls = new Hls();
        hls.loadSource(streamingUrl[0].url);
        hls.attachMedia(video);
        hls.on(Hls.Events.MANIFEST_PARSED, () => {
          video.play();
        });
      } else if (video.canPlayType("application/vnd.apple.mpegurl")) {
        video.src = streamingUrl[0].url;
        video.addEventListener("loadedmetadata", function () {
          video.play();
        });
      }
    }
  }, []);

  return (
    <div>
      {streamingUrl && streamingUrl.length > 0 ? (
        <video controls ref={videoRef} muted>
          <source src={streamingUrl[0].url} type="application/x-mpegURL" />
        </video>
      ) : (
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            height: "100%",
            color: "#FFFFFF",
            fontSize: "1.5rem",
          }}
        >
          <div>비디오를 불러오는 중...</div>
        </div>
      )}
    </div>
  );
};

export default VideoSection;
