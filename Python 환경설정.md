# tensorflow를 사용하여 AI를 학습 시키는 과정에서 계산량이 방대해 CPU로는 속도가 느리다는 문제를 해결하기 위해 GPU를 사용하여 문제를 해결하고자 한다.


#### 컴퓨터에 그래픽카드가 설치되어 있어야한다. 저자의 그래픽카드 스펙의 경우는 NVIDIA GeForce GTX 1050이다.


## 프로그램이 GPU상에서 병렬로 작동할 수 있도록 도와주는 프로그램들을 설치한다.
1.  그래픽카드의 스펙에 맞는 NVIDIA® GPU 드라이버를 설차한다.
- https://www.nvidia.com/download/index.aspx?lang=en-us
2. cmd화면에 nvidia-smi를 쳐서 CUDA Version을 확인하여 CUDA® Toolkit을 설치한다.
- https://developer.nvidia.com/cuda-toolkit-archive
3.  cuDNN SDK 8.1.0 다운 및 CUDA 설치 폴더로 덮어써야한다
- https://developer.nvidia.com/cudnn
  - 기본경로의 경우 C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\"각자 버전"\으로 들어가 다운 받은 폴더를 덮어쓴다.
> 추가로 nvidia-smi를 치면 그래픽카드의 VRAM을 확인 할 수 있는데 저자의 경우에는 4GB로 OOM 에러가 발생한다.


## tensorflow-gpu를 설치한다.
1. 현재(2023년 1월 26일) 기준으로 버전이 2.6까지밖에 나오지 않아 tensorflow 버전을 2.10에서 2.6으로 다운그래이드를 해야한다.
2. tensorflow 2.6 버전은 Python 3.12에서는 구동하지 않아 3.9버전으로 다운그래이드 해야한다.
3. numpy 1.23.4버전 이후로는 np.object라는 변수를 지원하지 않아 다운그레이드 해야한다.


```anaconda
conda install python=3.9
conda install -c anaconda tensorflow-gpu=2.6.0
```
> tensorflow-gpu 버전은 본인 환경에 맞게 설정, python=3.9
>
>  -c anaconda를 꼭 붙여줘야 함
>
> 위 코드 실행 시 아래 사진처럼 설치할 패키지 중 cuda와 cudnn이 포함되어 있음


## 환경 설정을 마친 이후에는 GPU를 잘 인식하는지 확인하기 위해 코드를 실행시켜본다.
```python
from tensorflow.python.client import device_lib

print(device_lib.list_local_devices())
```
다음과 같이 사용가능한 CPU와 GPU가 화면에 출력되면 성공이다.
```
[name: "/device:CPU:0"
device_type: "CPU"
memory_limit: 268435456
locality {
}
incarnation: 10636452296535545672
, name: "/device:GPU:0"
device_type: "GPU"
memory_limit: 2917295719
locality {
  bus_id: 1
  links {
  }
}
incarnation: 13121270483606858686
physical_device_desc: "device: 0, name: NVIDIA GeForce GTX 1050, pci bus id: 0000:01:00.0, compute capability: 6.1"
]
```




참고한 링크
> https://datastory-ok.tistory.com/63
