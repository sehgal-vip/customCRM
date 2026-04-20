import { useEffect, useState } from "react";
import { X, Download, Loader2 } from "lucide-react";
import { attachmentsApi } from "../../services/apiEndpoints";
import { useDialog } from "../../hooks/useDialog";

interface AttachmentPreviewModalProps {
  open: boolean;
  onClose: () => void;
  attachmentId: number | null;
  fileName: string;
}

const IMAGE_EXTENSIONS = [".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg", ".bmp"];
const PDF_EXTENSIONS = [".pdf"];

function getExtension(name: string): string {
  const dot = name.lastIndexOf(".");
  return dot >= 0 ? name.slice(dot).toLowerCase() : "";
}

export default function AttachmentPreviewModal({
  open,
  onClose,
  attachmentId,
  fileName,
}: AttachmentPreviewModalProps) {
  const dialogRef = useDialog(open);
  const [blobUrl, setBlobUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  const ext = getExtension(fileName);
  const isImage = IMAGE_EXTENSIONS.includes(ext);
  const isPdf = PDF_EXTENSIONS.includes(ext);
  const canPreview = isImage || isPdf;

  // Render-time reset when attachment changes
  const [prevFetchKey, setPrevFetchKey] = useState<string | null>(null);
  const fetchKey = open && attachmentId ? `${attachmentId}-${fileName}` : null;
  if (fetchKey && fetchKey !== prevFetchKey) {
    setPrevFetchKey(fetchKey);
    setLoading(true);
    setError(false);
    setBlobUrl(null);
  }
  if (!fetchKey && prevFetchKey) {
    setPrevFetchKey(null);
  }

  useEffect(() => {
    if (!open || !attachmentId) return;

    const localExt = getExtension(fileName);
    const localCanPreview = IMAGE_EXTENSIONS.includes(localExt) || PDF_EXTENSIONS.includes(localExt);

    let revoked = false;

    attachmentsApi
      .download(attachmentId)
      .then(({ blob }) => {
        if (revoked) return;
        const url = URL.createObjectURL(blob);
        setBlobUrl(url);
        setLoading(false);

        // If not previewable, trigger immediate download
        if (!localCanPreview) {
          triggerDownload(url, fileName);
          onClose();
        }
      })
      .catch(() => {
        if (!revoked) {
          setError(true);
          setLoading(false);
        }
      });

    return () => {
      revoked = true;
      setBlobUrl((prev) => {
        if (prev) URL.revokeObjectURL(prev);
        return null;
      });
    };
  }, [open, attachmentId, fileName, onClose]);

  const handleDownload = () => {
    if (blobUrl) triggerDownload(blobUrl, fileName);
  };

  if (!open || !attachmentId) return null;

  // For non-previewable files, show nothing (auto-downloads and closes)
  if (!canPreview && !loading && !error) return null;

  return (
    <dialog
      ref={dialogRef}
      onCancel={onClose}
      className="backdrop:bg-black/70 bg-transparent p-0 m-auto rounded-2xl max-w-4xl w-full"
    >
      <div className="bg-white rounded-2xl shadow-[0_25px_50px_rgba(0,0,0,0.25)] max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-3 border-b border-border">
          <h2 className="text-sm font-semibold text-text-primary truncate max-w-[80%]">{fileName}</h2>
          <div className="flex items-center gap-2">
            {blobUrl && (
              <button
                type="button"
                onClick={handleDownload}
                className="p-1.5 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
                title="Download"
              >
                <Download className="h-4 w-4" />
              </button>
            )}
            <button
              type="button"
              onClick={onClose}
              className="p-1.5 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-auto flex items-center justify-center min-h-[300px] bg-slate-50">
          {loading && (
            <div className="flex flex-col items-center gap-2 text-text-muted">
              <Loader2 className="h-8 w-8 animate-spin" />
              <p className="text-sm">Loading preview...</p>
            </div>
          )}

          {error && (
            <div className="text-center text-sm text-danger">
              <p>Failed to load file.</p>
            </div>
          )}

          {!loading && !error && blobUrl && isImage && (
            <img
              src={blobUrl}
              alt={fileName}
              className="max-w-full max-h-[75vh] object-contain"
            />
          )}

          {!loading && !error && blobUrl && isPdf && (
            <iframe
              src={blobUrl}
              title={fileName}
              className="w-full h-[75vh] border-0"
            />
          )}
        </div>
      </div>
    </dialog>
  );
}

function triggerDownload(url: string, fileName: string) {
  const a = document.createElement("a");
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}
