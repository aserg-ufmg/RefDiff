export function createBridge(contentWindow: any, wall?: Wall): FrontendBridge {
  if (wall == null) {
    wall = {
      listen(fn) {
        // $FlowFixMe[missing-local-annot]
        const onMessage = ({data}) => {
          fn(data);
        };
        window.addEventListener('message', onMessage);
        return () => {
          window.removeEventListener('message', onMessage);
        };
      },
      send(event: string, payload: any, transferable?: Array<any>) {
        contentWindow.postMessage({event, payload}, '*', transferable);
      },
    };
  }

  return (new Bridge(wall): FrontendBridge);
}
