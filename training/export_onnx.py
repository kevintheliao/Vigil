import glob
import os
from optimum.onnxruntime import ORTModelForSequenceClassification, ORTQuantizer
from optimum.onnxruntime.configuration import AutoQuantizationConfig
from transformers import AutoTokenizer

MODEL_DIR = "./model"
ONNX_DIR = "./model_onnx"
QUANTIZED_DIR = "./model_onnx_quantized"

#export the fine-tuned PyTorch model to ONNX format
model = ORTModelForSequenceClassification.from_pretrained(MODEL_DIR, export=True)
model.save_pretrained(ONNX_DIR)

tokenizer = AutoTokenizer.from_pretrained(MODEL_DIR)
tokenizer.save_pretrained(ONNX_DIR)

#quantize to 8-bit weights (arm64 config targets phone-class CPUs) to
#shrink the model for shipping in the app
quantizer = ORTQuantizer.from_pretrained(ONNX_DIR)
qconfig = AutoQuantizationConfig.arm64(is_static=False, per_channel=False)
quantizer.quantize(save_dir=QUANTIZED_DIR, quantization_config=qconfig)
tokenizer.save_pretrained(QUANTIZED_DIR)

def total_size_mb(directory):
    total = sum(os.path.getsize(f) for f in glob.glob(f"{directory}/*.onnx"))
    return total / 1e6

print(f"Original ONNX size: {total_size_mb(ONNX_DIR):.1f} MB")
print(f"Quantized ONNX size: {total_size_mb(QUANTIZED_DIR):.1f} MB")
